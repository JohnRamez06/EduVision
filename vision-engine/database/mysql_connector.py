import mysql.connector
from mysql.connector import pooling
from config.settings import settings
pool = pooling.MySQLConnectionPool(pool_name="eduvision_pool", pool_size=5, host=settings.db_host, port=settings.db_port, database=settings.db_name, user=settings.db_user, password=settings.db_password)
def get_connection(): return pool.get_connection()
