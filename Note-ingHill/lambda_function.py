import json
import boto3
import time
import urllib.parse
import datetime


s3_client = boto3.client('s3')

def lambda_handler(event, context):
    #Store type of file uploaded to S3 bucket
    filetype=""
    
    # Get the object from the event and show its content type
    bucket = event['Records'][0]['s3']['bucket']['name']
    key = urllib.parse.unquote_plus(event['Records'][0]['s3']['object']['key'], encoding='utf-8')
    try:
        s3_response = s3_client.get_object(Bucket=bucket, Key=key)
        print(s3_response)
        filetype=s3_response['ContentType']
        print(filetype)
    
    except Exception as e:
        print(e)
        print('Error getting object {} from bucket {}. Make sure they exist and your bucket is in the same region as this function.'.format(key, bucket))
        raise e
    
    # Check if type is JPEG Image -> Image-to-Text function call
    if filetype=="image/jpeg":
        r=img_txt(key)
        print("Image-to-Text: ",r)
    
    # Else if type is MP3 audio -> Speech-to-Text function call
    elif filetype=="audio/mpeg":
        r=spch_txt(key)
        print("Speech-to-Text: ",r)

    # JSON gets uploaded and recognised as binary/octet-stream MIME type
    elif filetype=="binary/octet-stream":
        trans = json.loads(s3_response['Body'].read().decode('utf-8'))
        text = trans['results']['transcripts'][0]['transcript']
        key_list = key.split('/')
        
        # Create new text file with transcribed text from JSON 
        file_name = "test.txt"
        lambda_path = "/tmp/" + file_name
        # key_list[1] is user ID
        s3_path = "public/"+key_list[1]+"/"+file_name   
        
        with open(lambda_path, 'w+') as file:
            file.write(text)
            file.close()
        # Upload file to S3 bucket
        s3 = boto3.resource('s3')
        s3.meta.client.upload_file(lambda_path, "nh-lecture-files200138-dev", s3_path)
        
        # Run text summarisation after waiting for upload
        time.sleep(5)
        r=summ(s3_path)
        print("Summarisation: ", r)
                
    else:
        print("None")


# Function to run Image-to-Text code on EC2 Instance    
def img_txt(key):
    # Create AWS Simple Systems Manager client
    ssm_client = boto3.client('ssm')
    # Send command to EC2 instance to run shell commands
    ssm_response = ssm_client.send_command(
            InstanceIds=['i-010850f8ec5ca6f48'],
            DocumentName="AWS-RunShellScript",
            Parameters={'commands': ['. /home/ubuntu/.profile','python3 /home/ubuntu/imgtext.py '+key],},
        )
    
    # Get command ID from SSM response
    command_id = ssm_response['Command']['CommandId']
    print("Command ID: ",command_id)
    
    # Code takes ~16s to execute on EC2 so keep trying while execution status is in progress (or exit after 10 tries)
    tries = 0
    output = 'Failed'
    while tries < 10:
        tries = tries + 1
        try:
            time.sleep(3)  # 3 second delay between tries
            result = ssm_client.get_command_invocation(
                CommandId=command_id,
                InstanceId="i-010850f8ec5ca6f48",
            )
            if result['Status'] == 'InProgress':
                print("Tries: ", tries)
                continue
            # Show output of Python code run on EC2
            output = result['StandardOutputContent']
            print('Error: ', result['StandardErrorContent'])
            break
        except ssm_client.exceptions.InvocationDoesNotExist:
            continue
    file_name = "test.txt"
    key_list = key.split('/')
    # key_list[1] is user ID
    s3_path = "public/"+key_list[1]+"/"+file_name   
    r=summ(s3_path)
    print("Summ: ",)
    return output
    
    
# Function to handle Speech-to-Text
def spch_txt(key):
    # AWS Transcribe client
    trans_client = boto3.client('transcribe')
    
    # URL of MP3 recording to be transcribed
    audio_file_url = "https://nh-lecture-files200138-dev.s3.ap-south-1.amazonaws.com/%s" % key
    
    key_list = key.split('/')   # ["public","userid","mp3 filename"]
    user_id = key_list[1]
    mp3_file =  key_list[2]
    
    # Timestamp appended to each Transcription Job and output JSON file
    date = datetime.datetime.now()
    timestamp = date.strftime("%Y-%m-%d-%H-%M-%S")
    
    job_name="SpeechtoText-"+str(timestamp)
    
    # Start a transcription job with language Indian English (en-IN)
    # Output to same S3 bucket location 
    trans_client.start_transcription_job(
        TranscriptionJobName=job_name,
        Media={'MediaFileUri':audio_file_url},
        MediaFormat='mp3',
        LanguageCode='en-IN',
        OutputBucketName='nh-lecture-files200138-dev',
        OutputKey="public/"+key_list[1]+"/"+str(job_name)+".json"
    )
    
    # Max number of tries
    tries=15
    while tries > 0:
        tries -= 1
        job = trans_client.get_transcription_job(TranscriptionJobName=job_name)
        status = job['TranscriptionJob']['TranscriptionJobStatus']
        if status in ['COMPLETED', 'FAILED']:
            print(f"Job {job_name} : {status}")
            
            if status == 'COMPLETED':
                return "Success"
            
            elif status == 'FAILED':
                return "Failed"
            
        else:
            print(f"{job_name} current status: {status}")
        time.sleep(5)
    
    return "Timeout"    

# Text summarisation function - Image and Speech
def summ(key):
    ssm_client = boto3.client('ssm')
    # Send command to EC2 instance to run shell commands
    ssm_response = ssm_client.send_command(
            InstanceIds=['i-010850f8ec5ca6f48'],
            DocumentName="AWS-RunShellScript",
            Parameters={'commands': ['. /home/ubuntu/.profile','cd /home/ubuntu/Note-ingHill-TextRank/','python3 /home/ubuntu/Note-ingHill-TextRank/textRank_python.py '+key],},
        )
    
    # Get command ID from SSM response
    command_id = ssm_response['Command']['CommandId']
    print("Summ Command ID: ",command_id)
    
    # Code takes ~16s to execute on EC2 so keep trying while execution status is in progress (or exit after 10 tries)
    tries = 0
    output = 'Failed'
    while tries < 10:
        tries = tries + 1
        try:
            time.sleep(3)  # 3 second delay between tries
            result = ssm_client.get_command_invocation(
                CommandId=command_id,
                InstanceId="i-010850f8ec5ca6f48",
            )
            if result['Status'] == 'InProgress':
                print("Tries: ", tries)
                continue
            # Show output of Python code run on EC2
            output = result['StandardOutputContent']
            print('Error: ', result['StandardErrorContent'])
            break
        except ssm_client.exceptions.InvocationDoesNotExist:
            continue

    return output
