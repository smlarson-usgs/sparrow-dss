__author__ = 'Andrew'


CREATE_TABLE = ("CREATE TABLE layer_cache "
                "(workspace text, layer text, completion_datetime text )"
                )


INSERT_DATA = ("INSERT INTO layer_cache(workspace, layer, completion_datetime) "
               "VALUES('{workspace}', '{layer}', '{completion_datetime}')"
               )