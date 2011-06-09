package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.datatable.impl.StandardDoubleColumnData;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.BinSet;
import gov.usgswim.sparrow.domain.InProcessBinSet;
import gov.usgswim.sparrow.request.BinningRequest.BIN_TYPE;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CalcEqualCountBins extends Action<BinSet> {
	
	private static final int DEFAULT_MAX_ALLOWED_ITERATIONS = 10000;
	private static final int DEFAULT_RESTART_MULTIPLIER = 7;
	private static final boolean USE_EQUAL_COUNT_STARTING_POSTS = true;
	
	private SparrowColumnSpecifier dataColumn;
	private double[] values;
	private BigDecimal minValue;
	private BigDecimal maxValue;
	private int numberOfRequestedBins;
	private BigDecimal detectionLimit;
	private Integer maxDecimalPlaces;
	private boolean useEqualCountStartPosts = USE_EQUAL_COUNT_STARTING_POSTS;
	private boolean bottomUnbounded;
	private boolean topUnbounded;
	
	
	//Settings and reports
	private double bestScore;	//The best score recorded (after the run is complete)
	private int totalIterations;	//Total number of iterations tried
	
	/**
	 *  max allowed number of iterations.  Directly tied to memory b/c each
	 * iteration records its state so it is not tried again.
	 */
	int maxAllowedIterations = DEFAULT_MAX_ALLOWED_ITERATIONS;
	
	/**
	 * The restartMultiplier times the number of bins is the number of times
	 * the main outer loop restarts the optimization search using the best
	 * post configuration found up to that point.  Restarting too many times
	 * means that a given pathway doesn't have time to settle to its best
	 * configuration.  Restarting too few times means that alternate pathways
	 * that may lead to better solutions are never tried.
	 */
	int restartMultiplier = DEFAULT_RESTART_MULTIPLIER;
	
	@Override
	public BinSet doAction() throws Exception {
		
		if (values == null) {
			values = buildSortedFilteredValues(dataColumn, detectionLimit);
		}
		
		if (minValue == null || maxValue == null) {
			if (values.length > 0) {
				Double min = values[0];
				Double max = values[values.length - 1];
				minValue = new BigDecimal(min);
				maxValue = new BigDecimal(max);
			} else {
				minValue = BigDecimal.ZERO;
				maxValue = BigDecimal.ZERO;
			}
		}

		//First generate equal range bins as a starting point
		CalcEqualRangeBins eqRangeAction = new CalcEqualRangeBins();
		eqRangeAction.setBinCount(numberOfRequestedBins);
		eqRangeAction.setDetectionLimit(detectionLimit);
		eqRangeAction.setMaxDecimalPlaces(maxDecimalPlaces);
		eqRangeAction.setDataColumn(dataColumn);
		
		BinSet eqRangeBinSet = eqRangeAction.run();
		
		BinSet resultBinSet = getEqualCountBins(values, eqRangeBinSet);
		Double variance = resultBinSet.getBinCountMaxVariancePercentage();
		
		if (variance > 10d) {
			ArrayList<BinSet> results = new ArrayList<BinSet>();
			results.add(resultBinSet);
			
			int tryAttempts = 0;	//Make two attempts
			boolean keepTrying = true;
			
			while (variance > 10d && tryAttempts < 2 && keepTrying) {
				BigDecimal cuv = resultBinSet.getCharacteristicUnitValue();
				int cuvScale = CalcEqualRangeBins.getScaleOfMostSignificantDigit(cuv);
				
				if (maxDecimalPlaces != null &&  cuvScale <= maxDecimalPlaces) {
					//We cannot make the CUV any smaller due to maxDecimalPlaces
					keepTrying = false;
					break;
				}
				
				//Ratchet the CUV one scale smaller
				BigDecimal newCUV = CalcEqualRangeBins.oneTimesTenToThePower((-1) * (cuvScale + 1));
				InProcessBinSet ipbs = resultBinSet.createInProcessBinSet();
				ipbs.characteristicUnitValue = newCUV;
				BinSet newStartBinSet = new BinSet(resultBinSet.getBins(), resultBinSet.getBinType(), ipbs, resultBinSet.getFormatPattern());
				resultBinSet = getEqualCountBins(values, newStartBinSet);
				results.add(resultBinSet);
				variance = resultBinSet.getBinCountMaxVariancePercentage();
				
				tryAttempts++;
			}
		}
		
		return resultBinSet;
	}
	
	/**
	 * 
	 * @param values	sorted and filtered values (filter out values below detect and specials)
	 * @param numberOfRequestedBins
	 * @param eqRangeBinSet
	 * @param detectionLimit
	 * @param characteristicUnitValue
	 * @return
	 * @throws Exception 
	 */
	protected BinSet getEqualCountBins(
			double[] values, BinSet eqRangeBinSet) throws Exception {
		
		InProcessBinSet inProcessBinSet = eqRangeBinSet.createInProcessBinSet();
		
		
		//index of bottom post we should keep b/c there is no reason to move the btm post.
		//Will be changed if a detection limit is ued.
		int bottomPostToKeep = inProcessBinSet.usesDetectionLimit? 1 : 0;	
		
		//Used for the actual calc process.
		//workingPosts starts & ends w/ the fixed top & btm posts we don't want
		//to change, but excludes the (possible) very bottom zero bin below the
		//detect limit.
		BigDecimal[] startingPosts = Arrays.copyOfRange(
				inProcessBinSet.posts, bottomPostToKeep, inProcessBinSet.posts.length);
		

		//Now the final value
		PostsWrapper optimizedPostsWrapper = seakBestPostConfiguration(
				values, startingPosts, eqRangeBinSet.getCharacteristicUnitValue());;
		BigDecimal[] optimizedPosts = optimizedPostsWrapper.posts;
		Integer[] binCounts = null;
		
		//slip in a extreme bottom post if we are using the detection limit
		if (inProcessBinSet.usesDetectionLimit) {
			BigDecimal[] postsWithBtm = new BigDecimal[optimizedPosts.length + 1];
			postsWithBtm[0] = eqRangeBinSet.getActualPostValues()[0];
			System.arraycopy(optimizedPosts, 0, postsWithBtm, 1, optimizedPosts.length);
			optimizedPosts = postsWithBtm;
			
			//Move the binCounts up one
			int[] orgBinCounts = optimizedPostsWrapper.binCounts;
			Integer[] binCountsWNonDetect = new Integer[orgBinCounts.length + 1];
			binCountsWNonDetect[0] = null;	//no easy way to get a count for non-detect bin
			for (int i = 0; i < orgBinCounts.length; i++) {
				binCountsWNonDetect[i+1] = orgBinCounts[i];
			}
			binCounts = binCountsWNonDetect;
		} else {
			//Reassign binCounts as Integer[]
			binCounts = ArrayUtils.toObject(optimizedPostsWrapper.binCounts);
		}
		
		inProcessBinSet.posts = optimizedPosts;
		
		inProcessBinSet.functional = CalcEqualRangeBins.createFunctionalPosts(inProcessBinSet.posts, 
				inProcessBinSet.actualMin, inProcessBinSet.actualMax,
				inProcessBinSet.characteristicUnitValue); 
		
		//Min and max values used to determine formatting.
		//Adjusted if the extreme top & bottom values are not visible b/c of
		//unlimited top/bottom bounds.
		BigDecimal formatMin = minValue;
		BigDecimal formatMax = maxValue;
		
		if (bottomUnbounded) formatMin = inProcessBinSet.posts[1];
		if (topUnbounded) formatMax = inProcessBinSet.posts[inProcessBinSet.posts.length - 2];
		
		
		DecimalFormat formatter = CalcEqualRangeBins.getFormat(
				formatMin, formatMax,
				CalcEqualRangeBins.getScaleOfMostSignificantDigit(inProcessBinSet.characteristicUnitValue), false);
		DecimalFormat functionalFormatter = CalcEqualRangeBins.getFormat(
				minValue, maxValue,
				CalcEqualRangeBins.getScaleOfMostSignificantDigit(inProcessBinSet.characteristicUnitValue), true);
		
		String formattedNonDetectLimit = "";
		
		if (inProcessBinSet.usesDetectionLimit) {
			DecimalFormat ndFormat = CalcEqualRangeBins.getFormat(detectionLimit, detectionLimit,
					CalcEqualRangeBins.getScaleOfMostSignificantDigit(detectionLimit), false);
			formattedNonDetectLimit = ndFormat.format(detectionLimit);
		}
		
		BinSet binSet = BinSet.createBins(inProcessBinSet, formatter, functionalFormatter,
				bottomUnbounded, topUnbounded, formattedNonDetectLimit, binCounts, BIN_TYPE.EQUAL_COUNT);
		
		return binSet;
	}
	
	protected PostsWrapper seakBestPostConfiguration(
			double[] values, BigDecimal[] startingPosts, 
			BigDecimal characteristicUnitValue) throws Exception {
		
		boolean debugIncludesPostValues = startingPosts.length < 12;
		boolean debugIncludesUniqueValueCount = startingPosts.length < 12;
		
		//Start with three different post configurations.  Depending on the vals,
		//any of them may be the best starting point.
		BigDecimal[][] startPostConfigs = null;
		
		if (useEqualCountStartPosts) {
			startPostConfigs = new BigDecimal[3][];
			
			startPostConfigs[0] = getExactEqualCountBins(values,
					numberOfRequestedBins, startingPosts[0], startingPosts[numberOfRequestedBins],
					characteristicUnitValue, true);
			if (startPostConfigs[0] == null) {
				startPostConfigs[1] = getExactEqualCountBins(values,
				numberOfRequestedBins, startingPosts[0], startingPosts[numberOfRequestedBins],
				characteristicUnitValue, false);
			}

			startPostConfigs[2] = startingPosts;

		} else {
			startPostConfigs = new BigDecimal[1][];
			
			startPostConfigs[0] = startingPosts;
		}
		
		//Init the attempted post configurations
		LinkedHashSet<PostsWrapper> attemptedPosts = new LinkedHashSet<PostsWrapper>();
		
		//Number of times to try the outer loop:
		//2X One less than the number of bins.  If 1 bin, don't attempt.
		//The number of attempts is not arbitrary:
		//For each starting state there are (bins - 1) number of 'larger' bins
		//that could be shrunk.  Each of those bins could be shrunk on either
		//side (2 X (bins-1)).  
		//Say we start at state A and narrowing the widest bin to arrive at state B.
		//a wide bin in B could be narrowed on the left or right.  Right would
		//eventually lead to the optimal solution, but left is arbitrarily chosen.
		//At that point the optimizer may try to go back to state B, but it is
		//prevented b/c it has already tried state B, thus preventing the
		//best optimization.
		//By feeding the best previous state back into the optimizer, we allow
		//it to continue on and choose Right, which it will do b/c the state
		//from choosing Left has already been tried.
		//int outerLoopAttempts = 2 * (startingPosts.length - 2);
		int outerLoopAttempts = restartMultiplier * startingPosts.length;
		int innerLoopAttempts = maxAllowedIterations/outerLoopAttempts;
		
		if (innerLoopAttempts < 20) innerLoopAttempts = 20;

		//Try starting from out three different start configs
		for (int i = 0; i < startPostConfigs.length; i++) {
			
			//Its possible that the ideal eq posts config may not have been
			//(easily) possible b/c it resulted in duplicate posts.  We are
			//ensured that the original eq range posts will not be null.
			if (startPostConfigs[i] != null) {
				PostsWrapper currentWrapper = new PostsWrapper(startPostConfigs[i]);
				int[][] binStats = getBinStats(values, startPostConfigs[i]);
				int[] binCounts = binStats[0];	//Number of values in each bin
				int[] binSplits = binStats[1];	//First value index NOT included in each bin
				currentWrapper.setBinCountsCopy(binCounts);
				currentWrapper.setBinSplits(binSplits);
				attemptedPosts.add(currentWrapper);
				
				if (log.isDebugEnabled()) {
					logPostState(values, currentWrapper, totalIterations, debugIncludesPostValues, debugIncludesUniqueValueCount);
				}
				
				runInnerLoop(currentWrapper, attemptedPosts,
						innerLoopAttempts, characteristicUnitValue);
			}
		}

		

		PostsWrapper lastWrapper = null;
		
		for (int j = 0; j < outerLoopAttempts; j++) {
			//This outer loop restarts the optimization hunt at the previously
			//best found state when the inner loop gets 'stuck'.

			
			//Find the current best config
			PostsWrapper currentWrapper = findBestAttemptedConfig(attemptedPosts);
			
			if (currentWrapper.equals(lastWrapper)) {
				log.debug("The best post configuration was already tried.");
				break;
			} else {
				lastWrapper = currentWrapper;
			}
			
			if (j > 0 && log.isDebugEnabled()) {
				log.debug("Outer loop #" + j + " attempt. Best score so far: " + currentWrapper.getScore());
			}

			runInnerLoop(currentWrapper, attemptedPosts,
					innerLoopAttempts, characteristicUnitValue);
			
		}

		
		//Now the final value
		PostsWrapper bestWrapper = findBestAttemptedConfig(attemptedPosts);
		if (log.isDebugEnabled()) {
			log.debug("Best Configuration Found:");
			logPostState(values, bestWrapper, totalIterations, debugIncludesPostValues, debugIncludesUniqueValueCount);
		}
		bestScore = bestWrapper.getScore();
		
		return bestWrapper;
	}
	
	protected void runInnerLoop(PostsWrapper startingWrapper,
			LinkedHashSet<PostsWrapper> attemptedPosts,
			int allowedLoopAttempts, BigDecimal characteristicUnitValue) {
		
		boolean debugIncludesPostValues = startingWrapper.posts.length < 12;
		boolean debugIncludesUniqueValueCount = startingWrapper.posts.length < 12;
		
		//Find the current best config
		PostsWrapper currentWrapper = startingWrapper;
		int[] binCounts = currentWrapper.getBinCountsCopy();
		int[] binSplits = currentWrapper.getBinSplitsCopy();
		
		for (int i = 0; i < allowedLoopAttempts; i++) {
			//This inner loop attempts to incrementally improve the bins
			//until no further option can be found that has not been tried.
			//It can get stuck in a situation like this:  |Count of values|
			//|8|8|8|........75.......|1|
			//where the 75 has already tried grabbing a value on either side.
			//(the 8s will be rejected as options to narrow b/c they do not
			//have a smaller neighbor).
			
			currentWrapper = tryToNarrowOneOfTheWiderBins(
					values, currentWrapper.posts, characteristicUnitValue, binCounts, binSplits, attemptedPosts);
			
			if (currentWrapper != null) {
				attemptedPosts.add(currentWrapper);
				
				//Fix the bin stats based on the affected bins
				int[] affectedBins = currentWrapper.getAffectedBins();

				for (int k = 0; k < affectedBins.length; k++) {
					int affectedBin = affectedBins[k];
					
					//The first index to include for bin 0 is 0 (indexed into the value array).
					//Otherwise, it is always the split value of the left-next bin.
					int firstIndexToInclude = (affectedBin == 0)?0:binSplits[affectedBin - 1];
					int[] modBinStats = this.getBinStat(values, currentWrapper.posts,
							firstIndexToInclude, affectedBin);
					binCounts[affectedBin] = modBinStats[0];
					binSplits[affectedBin] = modBinStats[1];
					
					
				}
				
				currentWrapper.setBinCountsCopy(binCounts);
				currentWrapper.setBinSplits(binSplits);
				
				totalIterations++;
				if (log.isDebugEnabled()) {
					logPostState(values, currentWrapper, totalIterations, debugIncludesPostValues, debugIncludesUniqueValueCount);
				}
				
			} else {
				break;
			}
		}
	}
	
	/**
	 * Finds the best configuration of those already tried
	 * 
	 * @param attemptedConfigurations
	 * @return
	 */
	protected PostsWrapper findBestAttemptedConfig(LinkedHashSet<PostsWrapper> attemptedConfigurations) {
		
		PostsWrapper bestWrap = null;
		double bestScore = Double.MAX_VALUE;	//lower is better;
		for (Iterator<PostsWrapper> iterator = attemptedConfigurations.iterator(); iterator.hasNext();) {
			PostsWrapper wrap = iterator.next();
			double score = wrap.getScore();
			if (score < bestScore) {
				bestScore = score;
				bestWrap = wrap;
			}
		}
		
		return bestWrap;
	}
	
	/**
	 * 
	 * @param values
	 * @param ipbs
	 * @param binToCalc
	 * @param firstValueIndexInThisBin The index of the first value included in this bin
	 * @return
	 */
	private PostsWrapper tryToNarrowOneOfTheWiderBins(
			double[] values, BigDecimal[] posts, BigDecimal cuv, int[] binCounts,
			int[] binSplits, HashSet<PostsWrapper> attempts) {
		
		

		int[] binsByCount = getBinsOrderedByCounts(binCounts);
		
		PostsWrapper newPosts = null;
		
		//Try narrowing bins, starting w/ the largest on down to the 2nd to smallest.
		for (int binByCountIndex = binsByCount.length - 1; binByCountIndex > 0; binByCountIndex--) {
			
			int binToNarrow = binsByCount[binByCountIndex];
			
			newPosts = tryToNarrow(values, posts,
					binCounts, binSplits, cuv, binToNarrow, attempts);
			
			if (newPosts != null) break;
		}
		
		return newPosts;
	}
	
	/**
	 * Returns a new set of proposed posts as a result of trying to narrow
	 * the specified bin.
	 * 
	 * Narrowing follows these rules<ul>
	 * <li>Narrowing is only done if an adjacent bin is smaller than the bin being narrowed.
	 * <li>Narrowing will prefer to narrow in the direction of the smaller adjacent bin,
	 * giving additional value counts to the smaller adjacent bin.
	 * <li>Narrowing that results in an already attempted configuration are rejected.
	 * <li>If, after attempting to narrow on the smallest side, the config is
	 * 		found to be a duplicate, the other side will be tried, provided
	 * 		it is smaller than the bin to be narrowed.
	 * 
	 * @param values
	 * @param posts
	 * @param binCounts
	 * @param binSplits
	 * @param cuv
	 * @param indexOfBinToNarrow
	 * @param attempts
	 * @return
	 */
	protected PostsWrapper tryToNarrow(double[] values, BigDecimal[] posts,
			int[] binCounts, int[] binSplits, BigDecimal cuv,
			int indexOfBinToNarrow, HashSet<PostsWrapper> attempts) {
		
		if (binCounts.length <= 1) {
			throw new IllegalArgumentException("Cannot narrow bins if there is only a single bin");
		}
		
		int countLeft = Integer.MAX_VALUE;	//The count of the bin to the right
		int countRight = Integer.MAX_VALUE;	//The count of the bin to the left
		
		
		if (indexOfBinToNarrow > 0) {
			countLeft = binCounts[indexOfBinToNarrow - 1];
		}
		
		if (indexOfBinToNarrow < binCounts.length - 1) {
			countRight = binCounts[indexOfBinToNarrow + 1];
		}
		
		//Bins may look like this;
		//   |..|..Current Bin ..|.......|
		//shrinkFromLeft means move the bound on the left of the current bin
		//to give extra values to the left bin.
		boolean shrinkFromLeftOK = countLeft < binCounts[indexOfBinToNarrow];
		boolean shrinkFromRightOK = countRight < binCounts[indexOfBinToNarrow];
		boolean shrinkFromLeftPrefered = shrinkFromLeftOK && countLeft <= countRight;

		PostsWrapper proposedPosts = null;
		
		if (shrinkFromLeftOK || shrinkFromRightOK) {
			proposedPosts = tryToNarrow(values, posts, binSplits, cuv,
					indexOfBinToNarrow, shrinkFromLeftPrefered);
			
			
			if (proposedPosts != null && attempts.contains(proposedPosts)) {
				//This configuration was already tried, but we can shrink from the other side
				proposedPosts = null;
				
				if (shrinkFromLeftOK && shrinkFromRightOK) {
					//both sides are ok, so try the other
					proposedPosts = tryToNarrow(values, posts, binSplits, cuv,
							indexOfBinToNarrow, ! shrinkFromLeftPrefered);
					
					if (proposedPosts != null && attempts.contains(proposedPosts)) {
						//This was also already tried.  Bummer.
						proposedPosts = null;
					}
				}
				
			}  else {
				//Note:  If the post could not be shrunk on one side cue to
				//contiguous values, it would not work to try moving the other
				//side post.
			}
		}

		return proposedPosts;	
	}
	
	
	
	
	/**
	 * Narrows by moving either the left or right post inward.
	 * 
	 * If successful, the a new (detached) set of posts is returned in a wrapper.
	 * 
	 * This functionality (left or right) is bundled to allow the caller to be
	 * more compact.
	 * 
	 * @param values
	 * @param posts
	 * @param binSplits
	 * @param cuv
	 * @param indexOfBinToNarrow
	 * @param shrinkFromLeft
	 * @return
	 */
	protected PostsWrapper tryToNarrow(double[] values, BigDecimal[] posts,
			int[] binSplits, BigDecimal cuv, int indexOfBinToNarrow, boolean shrinkFromLeft) {
		
		if (shrinkFromLeft) {
			int indexOfPostToMove = indexOfBinToNarrow;
			return tryToMovePostRight(values, posts,
					binSplits, cuv, indexOfPostToMove);
		} else {
			int indexOfPostToMove = indexOfBinToNarrow + 1;
			return tryToMovePostLeft(values, posts,
					binSplits, cuv, indexOfPostToMove);
		}
	}
	/**
	 * Tries to move the specified post right to include larger values.
	 * 
	 * If this method returns a PostsWrapper, it was able to move the post.
	 * If null, the post could not be moved because that move would cause the
	 * post to go above or below the value of an adjacent post.
	 * If successful, a copy of the post array is created (the original is never
	 *  modified).
	 * 
	 * @param values The values to bin
	 * @param posts The current fence post values (must be multiples of CUV).
	 * @param binSplits The index of the first value not included in each bin (points to values).
	 * @param cuv The unit value that post values must be a multiple of (keeps 'nice' values).
	 * @param indexOfPostToMove The most to move right.
	 * @return
	 */
	protected PostsWrapper tryToMovePostRight(double[] values, BigDecimal[] posts,
			int[] binSplits, BigDecimal cuv, int indexOfPostToMove) {
		
		if (indexOfPostToMove == 0) {
			throw new IllegalArgumentException("Cannot attempt to move the left-most (lowest) post at index 0");
		} else if (indexOfPostToMove >= posts.length - 1) {
			throw new IllegalArgumentException("Cannot attempt to move the right-most (largest) post.");
		}
		
		int binThatWillGrow = indexOfPostToMove - 1;
		BigDecimal post = posts[indexOfPostToMove];
		BigDecimal nextPostValueToRight = posts[indexOfPostToMove + 1];	//The next post right
		BigDecimal nextValueToInclude = new BigDecimal(values[binSplits[binThatWillGrow]]);
		
		BigDecimal numberOfCUVsToAdd = nextValueToInclude.subtract(post).
				divideToIntegralValue(cuv).add(BigDecimal.ONE);
		BigDecimal newPost = numberOfCUVsToAdd.multiply(cuv).add(post);
		
		if (newPost.compareTo(nextPostValueToRight) < 0) {
			//The new post is still less than the next post up the line, so its OK
			BigDecimal[] newPosts = Arrays.copyOf(posts, posts.length);
			newPosts[indexOfPostToMove] = newPost.stripTrailingZeros();
			PostsWrapper wrap = new PostsWrapper(newPosts, binThatWillGrow);
			return wrap;
		} else {
			//Sorry - moving the post right to include the next value results in
			//the posts crossing values.
			return null;
		}
	}
	
	
	/**
	 * Tries to move the specified post left to include smaller values.
	 * 
	 * If this method returns a PostsWrapper, it was able to move the post.
	 * If null, the post could not be moved because that move would cause the
	 * post to go above or below the value of an adjacent post.
	 * If successful, a copy of the post array is created (the original is never
	 *  modified).
	 * 
	 * @param values The values to bin
	 * @param posts The current fence post values (must be multiples of CUV).
	 * @param binSplits The index of the first value not included in each bin (points to values).
	 * @param cuv The unit value that post values must be a multiple of (keeps 'nice' values).
	 * @param indexOfPostToMove The most to move right.
	 * @return
	 */
	protected PostsWrapper tryToMovePostLeft(double[] values, BigDecimal[] posts,
			int[] binSplits, BigDecimal cuv, int indexOfPostToMove) {
		
		if (indexOfPostToMove == 0) {
			throw new IllegalArgumentException("Cannot attempt to move the left-most (lowest) post at index 0");
		} else if (indexOfPostToMove >= posts.length - 1) {
			throw new IllegalArgumentException("Cannot attempt to move the right-most (largest) post.");
		}
		
		int binThatWillShrink = indexOfPostToMove - 1;
		BigDecimal post = posts[indexOfPostToMove];
		BigDecimal nextPostValueToLeft = posts[indexOfPostToMove - 1];	//The next post to the left
		BigDecimal nextValueToInclude = new BigDecimal(values[binSplits[binThatWillShrink] - 1]);
		
		BigDecimal rangeToInclude = post.subtract(nextValueToInclude);
		BigDecimal[] numberOfCUVsToSubtract = rangeToInclude.divideAndRemainder(cuv);
		if (numberOfCUVsToSubtract[1].compareTo(BigDecimal.ZERO) > 0) {
			numberOfCUVsToSubtract[0] = numberOfCUVsToSubtract[0].add(BigDecimal.ONE);
		}

		BigDecimal newPost = post.subtract(numberOfCUVsToSubtract[0].multiply(cuv));
		
		if (newPost.compareTo(nextPostValueToLeft) > 0) {
			//The new post is still greater than the next post to the left, so its OK
			BigDecimal[] newPosts = Arrays.copyOf(posts, posts.length);
			newPosts[indexOfPostToMove] = newPost.stripTrailingZeros();
			PostsWrapper wrap = new PostsWrapper(newPosts, binThatWillShrink);
			return wrap;
		} else {
			//Sorry - moving the post left to include the next value results in
			//the posts crossing values.
			return null;
		}
	}
	
	/**
	 * Returns an array of zero based bin indexes in the order of the number of
	 * values contained in those bins.  The index of the bin containing the
	 * least number of values is first (index 0).
	 * 
	 * Note that the returned array contains a list of BIN indexes, not post indexes.
	 * The bottom bin is inclusive, the top exclusive except for the top bin.
	 * 
	 * @param Array in which the each indexed position contains the number of values
	 * 	found in that bin. (See getBinCounts() to create this list).
	 * @return
	 */
	protected int[] getBinsOrderedByCounts(int[] binCounts) {
		//Initial list in which the each indexed position contains the number
		//of values found in that bin. (not visa versa, which is to be returned).

		int[] result = new int[binCounts.length];
		
		//Key = The number of values in the bin
		//Value = A list of bin indexes that have [key] number of values
		TreeMap<Integer, ArrayList<Integer>> rank = new TreeMap<Integer, ArrayList<Integer>>();
		
		for (int i = 0; i < binCounts.length; i++) {
			if (rank.containsKey(binCounts[i])) {
				//This count value is already in the treeMap.
				ArrayList<Integer> list = rank.get(binCounts[i]);
				list.add(i);
			} else {
				//add new
				ArrayList<Integer> list = new ArrayList<Integer>(1);
				list.add(i);
				rank.put(binCounts[i], list);
			}
		}
		
		int resultIndex = 0;
		for (ArrayList<Integer> binIndexes : rank.values()) {
			for (Integer binIndex : binIndexes) {
				result[resultIndex] = binIndex;
				resultIndex++;
			}
		}
		
		return result;
	}
	
	/**
	 * Returns the count of values in each bin, as an array, and the index of
	 * where the splits between the bins occur.
	 * 
	 * The return structure is:
	 * [0][i] : Count of the number of values in bin i.
	 * [1][i] : Index of the first data value not included in bin i.
	 * 
	 * Index zero refers to the first bin.
	 * 
	 * The top and bottom post values must fully contain the
	 * data, otherwise incorrect values will be returned.
	 * 
	 * @return
	 */
	protected int[][] getBinStats(double[] values, BigDecimal[] posts) {
		
		//The number of value in each bin
		int[] valueCounts = new int[posts.length - 1];
		
		//The index of first value NOT included in each bin (similar to how String.substring indexes work).
		int[] binSplits = new int[posts.length - 1];
		
		int firstValueToInclude = 0;	//index into values of the first value to include in next bin
		
		for (int i = 0; i < valueCounts.length; i++) {
			int[] oneStat = getBinStat(values, posts, firstValueToInclude, i);
			valueCounts[i] = oneStat[0];
			binSplits[i] = oneStat[1];
			firstValueToInclude = binSplits[i];
		}
		
		return new int[][] {valueCounts, binSplits};
	}
	
	/**
	 * In-process method that returns the number of values in the specified bin
	 * and the index of the first value NOT included in the bin b/c it is too
	 * large.
	 * 
	 * This method is used for in-process calcs by getBinStats and is accurate
	 * for recalculating bin stats from left to right.
	 * 
	 * These two values are returned as an array as:
	 * [0] : Number of values in the bin
	 * [1] : Index of the first value NOT included in the bin (points to values[])
	 * 
	 * @param dataValues The data being binned
	 * @param posts The fence posts splitting the bins
	 * @param indexOfFirstValueToInclude The index of the first data value
	 * 		included in this bin.  Obviously, this value must be correct for the
	 * 		result to be correct.
	 * @param binIndex
	 * @return
	 */
	private int[] getBinStat(double[] dataValues, BigDecimal[] posts,
			int indexOfFirstValueToInclude, int binIndex) {
		
		double topPostValue = posts[binIndex + 1].doubleValue();
		int numberOfBins = posts.length - 1;
		
		int numberOfValuesInBin = 0;
		int firstValueIndexNotIncluded = 0;
		
		if (binIndex < numberOfBins - 1) {
			//any bin other than the top bin
			
			for (int j = indexOfFirstValueToInclude; j < dataValues.length; j++) {
				double currentValue = dataValues[j];
				if (currentValue >= topPostValue) {
					numberOfValuesInBin = j - indexOfFirstValueToInclude;
					firstValueIndexNotIncluded = j;
					break;
				} else if (j == dataValues.length - 1) {
					//Last value in list.  If not found at this point, the
					//post is larger than all values.
					numberOfValuesInBin = j - indexOfFirstValueToInclude + 1;
					firstValueIndexNotIncluded = j + 1;	//1 beyond j is first not included
				}
			}
		} else {
			//This is the top bin
			numberOfValuesInBin = dataValues.length - indexOfFirstValueToInclude;
			firstValueIndexNotIncluded = dataValues.length;
		}
		
		return new int[] {numberOfValuesInBin, firstValueIndexNotIncluded};
	}
	
	/**
	 * Copies the values out of the column, removes nulls, NaN and infinite values
	 * and sorts the values from low to high.
	 * 
	 * If a detection limit is included (non-null), values below the detection
	 * limit will not be included in the result.
	 * 
	 * @param data
	 * @param detectionLimit Value below which values should not be included.
	 * @return The cleaned and sorted data as a double[].
	 */
	protected double[] buildSortedFilteredValues(SparrowColumnSpecifier data,
			BigDecimal detectionLimit) {
		
		int totalRows = data.getRowCount();
		double[] tempResult = new double[totalRows];
		int count = 0;
		
		if (detectionLimit != null) {
			//1st implementation check the detection limit
			double dLimit = detectionLimit.doubleValue();
			
			//Extract all normal values to an array
			for (int r=0; r<totalRows; r++) {
				Double value = data.getDouble(r);
				
				if (value != null && !value.isNaN() && !value.isInfinite() && value >= dLimit) {
					tempResult[count] = value;
					count++;
				}
			}
		} else {
			//2nd implementation doesn't check the detection limit
			//Extract all normal values to an array
			for (int r=0; r<totalRows; r++) {
				Double value = data.getDouble(r);
				
				if (value != null && !value.isNaN() && !value.isInfinite()) {
					tempResult[count] = value;
					count++;
				}
			}
		}



		double[] values = Arrays.copyOf(tempResult, count);
		Arrays.sort(values);
		return values;
	}
	
	/**
	 * Returns an equal count set of bins so that the bins define break-point
	 * boundaries with approximately an equal number of values in each bin.
	 *
	 * @param data Table of data containing the column to divide into bins.
	 * @param columnIndex Index of the column to divide into bins.
	 * @param binCount Number of bins to divide the column into.
	 * @return Set of bins such that the bins define break-point boundaries
	 *         with an approximately equal number of values contained within.
	 */
	public static BigDecimal[] getExactEqualCountBins(double[] sortedData,
			int binCount, BigDecimal minPost, BigDecimal maxPost, BigDecimal cuv, boolean roundUp) {

		if (sortedData.length < (binCount * 2)) {
			//Give up - no point in try to find an exact equal count for so few values.
			return null;
		}
		
		int totalRows = sortedData.length;	//Total rows of data

		//ideal count of values in each bin.  This likely will not come out even,
		//so use a double to preserve the fractional rows.
		double binSize = (double)(totalRows) / (double)(binCount);

		//The first value is the lowest value in values[], the last value is the largest value.
		BigDecimal[] posts = new BigDecimal[binCount + 1];

		//Assign first and last values for the bins (min and max)
		posts[0] = minPost;
		posts[binCount] = maxPost;

		//Assign the middle breaks so that equal numbers of values fall into each bin
		for (int i=1; i<(binCount); i++) {

			//Get the row containing the nearest integer split
			double split = i * binSize;

			//The bin boundary is the value contained at that row.
			double topVal = sortedData[(int) Math.ceil(split)];
			double bottomVal = sortedData[(int) Math.floor(split)];
			BigDecimal newPost = null;
			
			if (roundUp) {
				newPost = CalcEqualRangeBins.roundToCUVAbove(new BigDecimal(bottomVal), cuv);
			} else {
				newPost = CalcEqualRangeBins.roundToCUVBelow(new BigDecimal(topVal), cuv);
			}
			posts[i] = newPost;
		}
		
		//Spread out the values if there are duplicates (bottom to top)
		BigDecimal lastValue = posts[0];
		//Note: posts has one more value than binCount.
		for (int i=1; i<binCount; i++) {

			//Move to the current value up to the last value + 1 CUV if the
			//current value is equal to the last value or is less than the last
			//value (can happen for detection limits).
			if (posts[i].compareTo(lastValue) <= 0) {
				posts[i] = lastValue.add(cuv);
			}
			
			lastValue = posts[i];
		}
		
		//Spread out the values if there are duplicates (top to bottom)
		//Note: posts has one more value than binCount.
		lastValue = posts[binCount];
		for (int i=(binCount - 1); i>0; i--) {

			//Move to the current value down to the last value - 1 CUV if the
			//current value is equal to the last value or is greater than the last
			//value (can happen if values are clustered near the top).
			if (posts[i].compareTo(lastValue) >= 0) {
				posts[i] = lastValue.subtract(cuv);
			}
			
			lastValue = posts[i];
		}
		
		//Do we still have overlapping or out of order bins?!
		lastValue = posts[0];
		boolean hasDuplicate = false;
		//Note: posts has one more value than binCount.
		for (int i=1; i <= binCount; i++) {

			if (posts[i].compareTo(lastValue) <= 0) {
				hasDuplicate = true;
				break;
			}
			
			lastValue = posts[i];
		}
		
		if (!hasDuplicate) {
			return posts;
		} else {
			//The spread-out operations do not ensure perfect spreads, so its possible
			//there will be duplicate post values.
			return null;
		}
		
	}
	
	/**
	 * Only used for testing as this directly sets the data to be processed.
	 * Normally this data would be filtered so that any values under the detection
	 * limit would not be included - use with care.
	 * @param values
	 */
	protected void setSortedAndFilteredValues(double[] values) {
		this.values = values;
	}
	
	/**
	 * Utility test method to set values that are unfiltered or sorted.
	 * 
	 * This method creates a SparrowColumnSpecifier to store the data, which
	 * ensures that the data will be sorted b/f attempting to process the data.
	 * @param values
	 */
	public void setUnsortedValues(double[] values) {
		
		StandardDoubleColumnData col = new StandardDoubleColumnData(values, "name", "units",
				"description",	null, false);
		
		SimpleDataTable table = new SimpleDataTable(new ColumnData[]{col}, "name",
				"description", null, null);
		
		SparrowColumnSpecifier scs = new SparrowColumnSpecifier(table, 0, null);
		dataColumn = scs;
	}
	
	/**
	 * A configuration of posts that has been tried by the Equal Count
	 * algorithm.
	 * 
	 * hashCode and equals properly reflect the state of the posts.
	 * 
	 * @author eeverman
	 */
	public static class PostsWrapper {
		
		Integer hash = null;
		BigDecimal[] posts;
		private int[] affectedBins = null;
		
		//
		//Required for debug and to be able to jump back to an earlier config
		private int[] binCounts = null;
		private int[] binSplits = null;

		public PostsWrapper(BigDecimal[] posts) {
			this.posts = posts;
		}
		
		public PostsWrapper(BigDecimal[] posts, int leftMostAffectedBin) {
			this.posts = posts;
			addLeftAndNextAffectedBin(leftMostAffectedBin);
		}
		
		@Override
		public int hashCode() {
			if (hash == null) {
				HashCodeBuilder hcb = new HashCodeBuilder(823, 482867);
				for (int i = 0; i < posts.length; i++) {
					hcb.append(posts[i]);
				}
				hash = hcb.hashCode();
			}
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof PostsWrapper) {
				PostsWrapper comp = (PostsWrapper) obj;
				return (comp.hashCode() == hashCode());
			}
			return false;
		}
		
		/**
		 * Records which bins were affected by the new posts.
		 * 
		 * This method always records two values:  The passed bin index and the
		 * next bin index (index + 1) b/c the bins are always affected in pairs.
		 * Thus, always pass the left-most affected bin.
		 * 
		 * @param leftMostAffectedBin
		 */
		public void addLeftAndNextAffectedBin(int leftMostAffectedBin) {
			if (affectedBins == null) {
				affectedBins = new int[2];
				affectedBins[0] = leftMostAffectedBin;
				affectedBins[1] = leftMostAffectedBin + 1;
			} else {
				ArrayUtils.add(affectedBins, leftMostAffectedBin);
				ArrayUtils.add(affectedBins, leftMostAffectedBin + 1);
				Arrays.sort(affectedBins);
			}
		}
		
		/**
		 * Returns the bins that were affected in morphing to this current
		 * post configuration.  The returned bins are always sorted from lowest
		 * to highest.
		 * 
		 * @return
		 */
		public int[] getAffectedBins() {
			return affectedBins;
		}
		
		/**
		 * Creates a score for the quality of the current post configuration.
		 * 
		 * Currently the score is the sum of the square of the distance of each
		 * bin size from the ideal bin size.  Or for n=0...last bin:
		 * Sum(abs(idealBinSize - binSize(n))^2)
		 * 
		 * @return
		 */
		public double getScore() {
			
			double score = 0;
			
			if (binCounts == null) {
				throw new IllegalStateException("binCounts were not assigned!");
			}
			
			int totalValues = 0;
			
			for (int i = 0; i < binCounts.length; i++) {
				totalValues = totalValues + binCounts[i];
			}
			
			double idealBinCnt = (double)totalValues / (double)(binCounts.length);
			
			for (int i = 0; i < binCounts.length; i++) {
				score+= Math.pow(Math.abs(idealBinCnt - binCounts[i]), 2);
			}
			
			return score;
			
		}

		/**
		 * Returns a copy of the bin counts.
		 * @return
		 */
		public int[] getBinCountsCopy() {
			return Arrays.copyOf(binCounts, binCounts.length);
		}

		/**
		 * Will copy the values so future modification to not reflect back into class.
		 * @param binCounts
		 */
		public void setBinCountsCopy(int[] binCounts) {
			this.binCounts = Arrays.copyOf(binCounts, binCounts.length);
		}

		/**
		 * Returns a copy of the bin splits.
		 * @return
		 */
		public int[] getBinSplitsCopy() {
			return Arrays.copyOf(binSplits, binSplits.length);
		}

		/**
		 * Will copy the values so future modification to not reflect back into class.
		 * @param binCounts
		 */
		public void setBinSplits(int[] binSplits) {
			this.binSplits = Arrays.copyOf(binSplits, binSplits.length);
		}
		
	}
	
	protected void logPostState(double[] values, PostsWrapper wrap, int totalIterationCount,
			boolean includePostValues, boolean includeUniqueValueCount) {
		
		if (values.length <= 150) {
			logPostStateFull(values, wrap, totalIterationCount, includePostValues);
			return;
		}
		
		int[] binCounts = wrap.getBinCountsCopy();
		//int[] binSplits = wrap.getBinSplitsCopy();
		
		double displayLength = 150;
		double valCount = values.length;
		double scaleFactor = displayLength / valCount;
		StringBuffer sb = new StringBuffer();
		
		//Add a score
		sb.append("[" + totalIterationCount + "][scr:").append(wrap.getScore()).append("]");
		
		//Unique Values
		if (includeUniqueValueCount) {
			sb.append("[");
			for (int i = 0; i < binCounts.length; i++) {
				sb.append(getUniqueValueCount(values, wrap, i)).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);	//rm the last comma
			
			sb.append("]");
		}
		
		//Add Posts
		if (includePostValues) {
			sb.append("[");
			for (int i = 0; i < wrap.posts.length; i++) {
				if (wrap.posts[i].compareTo(BigDecimal.ZERO) == 0) {
					sb.append("0.0,");	//Zeros can't seem to be printed nicely otherwise
				} else {
					sb.append(wrap.posts[i].toPlainString()).append(",");
				}
			}
			sb.deleteCharAt(sb.length() - 1);	//rm the last comma
			
			sb.append("]");
		}
		
		//Draw the bins
		sb.append("|");
		for (int i = 0; i < binCounts.length; i++) {
			String binLengthStr = "" + binCounts[i];
			BigDecimal binLength = new BigDecimal(((double) binCounts[i]) * scaleFactor);
			int intBinLength = CalcEqualRangeBins.round(binLength, 0, RoundingMode.HALF_EVEN).intValue();
			
			
			//Note: the initial starting configuration will have no affected bins.
			if (wrap.affectedBins != null && Arrays.binarySearch(wrap.affectedBins, i) > -1) {
				int dotLength = intBinLength - binLengthStr.length();
				if (dotLength < 0) dotLength = 0;
				
				int firstHalf = dotLength / 2;
				int secondHalf = dotLength - firstHalf;
				
				sb.append(StringUtils.repeat("*", firstHalf));
				sb.append(binLengthStr);
				sb.append(StringUtils.repeat("*", secondHalf));
			} else {
				int dotLength = intBinLength - binLengthStr.length();
				if (dotLength < 0) dotLength = 0;
				
				int firstHalf = dotLength / 2;
				int secondHalf = dotLength - firstHalf;
				
				sb.append(StringUtils.repeat(".", firstHalf));
				sb.append(binLengthStr);
				sb.append(StringUtils.repeat(".", secondHalf));
			}
			
			sb.append("|");
		}
		
		log.debug(sb.toString());
	}
	
	/**
	 * Renders the values 1 to 1 (no scaling), so don't use for large sets of values.
	 * @param values
	 * @param wrap
	 * @param includePostValues
	 * @param includeUniqueValueCount
	 */
	protected void logPostStateFull(double[] values, PostsWrapper wrap,
			int totalIterationCount, boolean includePostValues) {
		
		int[] binCounts = wrap.getBinCountsCopy();
		//int[] binSplits = wrap.getBinSplitsCopy();
		
		StringBuffer sb = new StringBuffer();
		
		//Add a score
		sb.append("[" + totalIterationCount + "][scr:").append(wrap.getScore()).append("]");
		
		//Add Posts
		if (includePostValues) {
			sb.append("[");
			for (int i = 0; i < wrap.posts.length; i++) {
				if (wrap.posts[i].compareTo(BigDecimal.ZERO) == 0) {
					sb.append("0.0,");	//Zeros can't seem to be printed nicely otherwise
				} else {
					sb.append(wrap.posts[i].toPlainString()).append(",");
				}
			}
			sb.deleteCharAt(sb.length() - 1);	//rm the last comma
			
			sb.append("]");
		}
		
		if (values.length > 0) {
			//Draw the bins
			sb.append("|");
			char[] valPoints = new char[] {'-', '_'};
			int valPointer = 0;
			double lastValue = values[0];
			for (int i = 0; i < binCounts.length; i++) {
	
				int firstValueIncluded = 0;
				int firstValueNotIncluded = wrap.getBinSplitsCopy()[i];
				
				if (i > 0) {	//Not the bottom bin
					firstValueIncluded = wrap.getBinSplitsCopy()[i - 1];
				}
				
				for (int j = firstValueIncluded; j < firstValueNotIncluded; j++) {
					double d = values[j];
					if (d == lastValue) {
						sb.append(valPoints[valPointer]);
					} else {
						valPointer++;
						if (valPointer > 1) valPointer = 0;
						lastValue = d;
						sb.append(valPoints[valPointer]);
					}
				}
				
				sb.append("|");
			}
		} else {
			sb.append(" [no values in the array to draw]");
		}
		
		log.debug(sb.toString());
	}
	
	/**
	 * For debug usage only.
	 * @param values
	 * @param wrap
	 * @param binIndex
	 * @return
	 */
	public static int getUniqueValueCount(double[] values, PostsWrapper wrap, int binIndex) {
		
		if (values.length == 0) return 0;
		
		int firstValueIncluded = 0;
		int firstValueNotIncluded = wrap.getBinSplitsCopy()[binIndex];
		
		if (binIndex > 0) {
			//Not the bottom bin
			firstValueIncluded = wrap.getBinSplitsCopy()[binIndex - 1];
		}
		
		HashSet<Double> set = new HashSet<Double>();
		for (int i = firstValueIncluded; i < firstValueNotIncluded; i++) {
			double d = values[i];
			set.add(d);
		}
		
		return set.size();
	}

	public void setDataColumn(SparrowColumnSpecifier dataColumn) {
		this.dataColumn = dataColumn;
	}

	public void setValues(double[] values) {
		this.values = values;
	}

	public void setMinValue(BigDecimal minValue) {
		this.minValue = minValue;
	}

	public void setMaxValue(BigDecimal maxValue) {
		this.maxValue = maxValue;
	}

	public void setBinCount(int binCount) {
		this.numberOfRequestedBins = binCount;
	}

	public void setDetectionLimit(BigDecimal detectionLimit) {
		this.detectionLimit = detectionLimit;
	}

	public void setMaxDecimalPlaces(Integer maxDecimalPlaces) {
		this.maxDecimalPlaces = maxDecimalPlaces;
	}

	/**
	 * Max allowed number of iterations.  Directly tied to memory b/c each
	 * iteration records its state so it is not tried again.
	 */
	public int getMaxAllowedIterations() {
		return maxAllowedIterations;
	}

	/**
	 * Max allowed number of iterations.  Directly tied to memory b/c each
	 * iteration records its state so it is not tried again.
	 * @param maxAllowedIterations
	 */
	public void setMaxAllowedIterations(int maxAllowedIterations) {
		this.maxAllowedIterations = maxAllowedIterations;
	}

	/**
	 * The restartMultiplier times the number of bins is the number of times
	 * the main outer loop restarts the optimization search using the best
	 * post configuration found up to that point.  Restarting too many times
	 * means that a given pathway doesn't have time to settle to its best
	 * configuration.  Restarting too few times means that alternate pathways
	 * that may lead to better solutions are never tried.
	 * @return
	 */
	public int getRestartMultiplier() {
		return restartMultiplier;
	}

	/**
	 * The restartMultiplier times the number of bins is the number of times
	 * the main outer loop restarts the optimization search using the best
	 * post configuration found up to that point.  Restarting too many times
	 * means that a given pathway doesn't have time to settle to its best
	 * configuration.  Restarting too few times means that alternate pathways
	 * that may lead to better solutions are never tried.
	 * 
	 * @param restartMultiplier
	 */
	public void setRestartMultiplier(int restartMultiplier) {
		this.restartMultiplier = restartMultiplier;
	}

	public double getBestScore() {
		return bestScore;
	}

	public double getTotalIterations() {
		return totalIterations;
	}

	public void setUseEqualCountStartPosts(boolean useEqualCountStartPosts) {
		this.useEqualCountStartPosts = useEqualCountStartPosts;
	}

	public boolean isUseEqualCountStartPosts() {
		return useEqualCountStartPosts;
	}

	public boolean isBottomUnbounded() {
		return bottomUnbounded;
	}

	public void setBottomUnbounded(boolean bottomUnbounded) {
		this.bottomUnbounded = bottomUnbounded;
	}

	public boolean isTopUnbounded() {
		return topUnbounded;
	}

	public void setTopUnbounded(boolean topUnbounded) {
		this.topUnbounded = topUnbounded;
	}
	
}
