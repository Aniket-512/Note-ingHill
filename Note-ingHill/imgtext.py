import sys
sys.path.append('/home/ubuntu/.local/bin/')
sys.path.append('/home/ubuntu/.local/lib/python3.8/site-packages') # Update python package from 3.6 to 3.8
sys.path.append('/home/ubuntu/.local/lib/python3.8/site-packages/pytesseract')
sys.path.append('home/ubuntu/.local/lib/python3.8/site-packages/cv2/')

import pytesseract
import botocore
import boto3
import cv2
import os

key = sys.argv[1]
key_list = key.split('/')

s3_client = boto3.client('s3')
try:
	s3_client.download_file('nh-lecture-files200138-dev', key,'img.jpg')
except botocore.exceptions.ClientError as e:
	if(e.response['Error']['Code']=='404'):
		print("Object does not exist")
	else:
		raise e

img = cv2.imread("img.jpg")
img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
cv2.waitKey(0)

str_out = pytesseract.image_to_string(img)

file1 = open("TextOutput.txt", "w")
file1.write(str_out)
file1.close()

with open("TextOutput.txt", "rb") as f:
	s3_client.upload_fileobj(f, "nh-lecture-files200138-dev","public/"+key_list[1]+"/test.txt")

f.close()

try:
	os.remove("TextOutput.txt")
	os.remove("img.jpg")
except OSError as e:
	print("Error: %s - %s." % (e.filename, e.strerror))

