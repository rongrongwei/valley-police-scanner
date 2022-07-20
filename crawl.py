from playwright.sync_api import sync_playwright
from bs4 import BeautifulSoup
import pandas as pd
import time
import sqlite3

# import pdb

import sqlite3
import time
import datetime
from sqlite3 import IntegrityError, ProgrammingError

# URL = ''
TABLE_NAME = 'scans'
SLEEP_MIN = 60*60*4
DB_FILE = 'data/valley_scanner.db'

CONN = sqlite3.connect(DB_FILE)
c = CONN.cursor()
c.execute(f"""CREATE TABLE IF NOT EXISTS {TABLE_NAME} (
            CallNumber TEXT PRIMARY KEY,
            Agency TEXT,
            Address TEXT,
            CallTime TEXT,
            OnSceneTime TEXT,
            ClosedTime TEXT,
            CallType TEXT
        )""")

CONN.commit()
c.close()
CONN.close()

fail_count = 0
while True:
    try:
        with sync_playwright() as p:
            browser = p.chromium.launch()
            page = browser.new_page()
            page.goto("https://www.arcgis.com/apps/dashboards/df9972f4a1f44b2289b600c73cb66c72", wait_until="networkidle")
            page.wait_for_timeout(5000)
            page.set_viewport_size({'width':1600, 'height':100000})
            html_data = page.inner_html('body')
            browser.close()
        fail_count = 0
    except:
        print(f"failed {fail_count} times in a row")
        fail_count += 1 
        time.sleep(120)
        continue

    soup = BeautifulSoup(html_data, features="lxml")
    table_elements = soup.find_all('table')
    if len(table_elements) == 0:
        fail_count += 1
        print("no data pulled?")
        print(f"failed {fail_count} times in a row")
        time.sleep(60) # wait a minute
        continue

    print("pulled at: " + str(datetime.datetime.now()))
    print(f"number of items pulled {len(table_elements)-1}")

    rows = []
    for elem in table_elements:
        t = elem.getText().strip()
        row = t.split('\n\n\n')
        if len(row) == 7:
            rows.append(row)
        else:
            row.append('')
            rows.append(row)

    CONN = sqlite3.connect(DB_FILE)
    c = CONN.cursor()
    redundant_count = 0
    for row in rows[1:]: # skip header row
        try:
            c.execute(f"INSERT INTO {TABLE_NAME} VALUES (?, ?, ?, ?, ?, ?, ?)", row)
            CONN.commit()
        except IntegrityError:
            redundant_count+=1
        except ProgrammingError:
            print("programming error")
            print(row)


    print(f"already had {redundant_count} rows")
    c.close()
    CONN.close()
    time.sleep(SLEEP_MIN)
#             if error_no > 10:
#                 print(i)
#                 break
#             html_data = page.inner_html('body')
#             soup = BeautifulSoup(html_data)
#             table_data = soup.find_all('table')
#             table_store += table_data
# 
# 
