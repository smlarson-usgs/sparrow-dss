Notes for running locally or debugging locally.

There are a few customization system properties that can optimize local operation.
Most are set in class gov.usgswim.sparrow.SparrowTestBase.doOneTimeGeneralSetup()

_________________________________________________________________________________________
_________________________________________________________________________________________
Constant Name: gov.usgswim.sparrow.cachefactory.PredictDataFactory.ACTION_IMPLEMENTATION_CLASS
Constant Value: (same)
= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 
Description:
Assigns the Action implementation class that will load predict data.
If not specified, the default production implementation is used, which loads
models from the db.  For debug, tests, or running a local server, this can be set to:
gov.usgswim.sparrow.action.LoadModelPredictDataFromFile
or
gov.usgswim.sparrow.action.LoadModelPredictDataFromSerializationFile
The LoadModelPredictDataFromFile implementation only loads model 50 from a set of text files
distributed w/ the project.
The LoadModelPredictDataFromSerializationFile attempts to read model data from a configured
directory of serialized PredictData objects.  Optionally, if a model is not found there,
it will fetch it from the db and serialize it there.

_________________________________________________________________________________________
_________________________________________________________________________________________
Constant Name: gov.usgswim.sparrow.action.LoadModelPredictDataFromSerializationFile.DATA_DIRECTORY
Constant Value: (same)
= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 
Specifies the directory this class should fetch and (optionally) store serialized
PredictData instances to.  If unassign and not explicitly set for an instance,
the java temp directory will be used.
_________________________________________________________________________________________
_________________________________________________________________________________________
Constant Name: gov.usgswim.sparrow.action.LoadModelPredictDataFromSerializationFile.FETCH_FROM_DB_IF_NO_LOCAL_FILE
Constant Value: (same)
= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 
If set to "true", this loader will fetch a model from the db and store it to the configured
data directory, if it is not found in the data directory.


_________________________________________________________________________________________
_________________________________________________________________________________________
Constant Name: gov.usgswim.sparrow.action.PredictionContextHandler.DISABLE_DB_ACCESS
Constant Value: gov.usgswim.sparrow.action.PredictionContextHandler.DisableDbAccess
= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 
Description:
Set a system property of this name to 'true' to disable db access.  All caching
will then only be done locally.  In production, when a PredictionContext is
created, it is written to the db so that other servers in the cluster have access
to it.  When running locally w/ a single server, this will only slow down your
testing.