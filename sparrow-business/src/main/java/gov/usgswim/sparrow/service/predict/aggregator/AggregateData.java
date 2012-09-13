package gov.usgswim.sparrow.service.predict.aggregator;

/**
 * A data structure used to contain information about a specific row of
 * aggregated data.
 */
public class AggregateData {
    private double[] data;
    private int count;

    public AggregateData(double[] data, int count) {
        this.data = data;
        this.count = count;
    }

    public double[] getData() {
        return data;
    }
    public void setData(double[] data) {
        this.data = data;
    }

    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
}