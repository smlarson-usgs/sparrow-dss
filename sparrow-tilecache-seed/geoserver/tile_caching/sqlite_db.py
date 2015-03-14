__author__ = 'Andrew'

import os
import sqlite3
from sql_statements import CREATE_TABLE, INSERT_DATA


def dict_factory(cursor, row):
    """
    Factory to turn database queries
    as a dictionary with column names.

    """
    d = {}
    for idx, col in enumerate(cursor.description):
        d[col[0]] = row[idx]
    return d


class SqliteDB(object):

    def __init__(self, db_path='/tmp/sparrow_caching.db'):
        self.db_path = db_path
        self.connection = None

    def create_db(self):
        conn = sqlite3.connect(self.db_path)
        c = conn.cursor()
        c.execute(CREATE_TABLE)
        conn.commit()
        conn.close()
        return 'Created SQLite database.'

    def destroy_db(self):
        os.remove(self.db_path)

    def insert_data(self, workspace, layer, complete_datetime):
        conn = sqlite3.connect(self.db_path)
        c = conn.cursor()
        insert_statement = INSERT_DATA.format(workspace=workspace,
                                              layer=layer,
                                              completion_datetime=complete_datetime
                                              )
        c.execute(insert_statement)
        conn.commit()
        conn.close()
        return insert_statement

    def query_db(self, workspace=None):
        self.connection = sqlite3.connect(self.db_path)
        self.connection.row_factory = dict_factory
        c = self.connection.cursor()
        if workspace is not None:
            query_statement = "SELECT * FROM layer_cache lc WHERE lc.workspace='{workspace}'".format(workspace=workspace)
        else:
            query_statement = "SELECT * FROM layer_cache"
        query_result = c.execute(query_statement)
        return query_result.fetchall()

    def close_db(self):
        self.connection.close()


if __name__ == '__main__':
    DB_PATH = '/Users/Andrew/Desktop/expt/sparrow.db'
    sql3 = SqliteDB(db_path=DB_PATH)
    sql3.create_db()
    sql3.insert_data('sparrow-catchments', '34N3902903', '03/14/2015 11:48')
    query_result = sql3.query_db('sparrow-catchments')
    print(query_result)
    sql3.close_db()
    sql3.destroy_db()