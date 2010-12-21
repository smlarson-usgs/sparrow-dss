An initial performance / Startup Script

-- Use after a redeploy to force all models into memory --
The test that reads all the models on the home page, then requests a context, reach tile, and map tile from each.  It then repeats so that initial request times can be compared to the 2nd request time.  After a redeploy, run this test to ensure that all model data and geom have been forced into memory.