# Aniket - Connecting AWS
import sys
import boto3
import os

sys.path.append('/home/ubuntu/.local/bin/')
sys.path.append('/home/ubuntu/.local/lib/python3.8/site-packages')
sys.path.append('/home/ubuntu/nltk-data/tokenizers/punkt/PY3')

# Import the libraries
import numpy as np
import pandas as pd
import nltk
import re
from sklearn.metrics.pairwise import cosine_similarity
import networkx as nx
from nltk.corpus import stopwords
from nltk.tokenize import sent_tokenize

# Reading S3 key from command line as arg
key = str(sys.argv[1])
key_list = key.split('/')
# Local file name - somefile.txt
localname = key_list[2]

# Download transcribed text from S3 bucket
s3_client = boto3.client('s3')
try:
        s3_client.download_file('nh-lecture-files200138-dev', key, localname)
except botocore.exceptions.ClientErrors as e:
        if(e.response['Error']['Code']=='404'):
                print("Object does not exist")
        else:
                raise e

# Open text file and read
file = open(localname, encoding="utf-8")
text = file.read()

# Getting the sentences

sentences = sent_tokenize(text)

# Remove punctuations, numbers and special characters

clean_sentences = pd.Series(sentences).str.replace("[^a-zA-Z]", " ")

# Make alphabets lowercase

clean_sentences = [s.lower() for s in clean_sentences]

# Getting the stopwords ('and', 'to' etc.) so that it can be removed
# Stopwords are removed so that more important parts of the sentence can be focussed on

stop_words = stopwords.words('english')

# Function to remove stopwords

def remove_stopwords(sen):
    sen_new = " ".join([i for i in sen if i not in stop_words])
    return sen_new

# Remove stopwords from the sentences

clean_sentences = [remove_stopwords(r.split()) for r in clean_sentences]

# Extract word vectors using glove

word_embeddings = {}
f = open('glove.6B.100d.txt', encoding='utf-8')
for line in f:
    values = line.split()
    word = values[0]
    coefs = np.asarray(values[1:], dtype='float32')
    word_embeddings[word] = coefs
f.close()

# Extract sentence vectors

sentence_vectors = []
for i in clean_sentences:
    if len(i) != 0:
        v = sum([word_embeddings.get(w, np.zeros((100,))) for w in i.split()])/(len(i.split()))
    else:
        v = np.zeros((100,))
    sentence_vectors.append(v)

# Similarity matrix (intialised as a square matrix of 0s)
# The size of this matrix will be n x n, where n is the number of sentences

sim_mat = np.zeros([len(sentences), len(sentences)])

# Cosine similarity of a sentence is calculated with every other sentence and stored in the sim_mat matrix
# Cosine similarity is the dot product of the vector values of the sentences
# Higher this value, stronger is the relationship between the 2 sentences (Smaller theta value)
# This is not done for the same sentence as we aren't interested in the relationship of a sentence with itself 
# Hence, it is kept as 0

for i in range(len(sentences)):
      for j in range(len(sentences)):
        if i != j:
              sim_mat[i][j] = cosine_similarity(sentence_vectors[i].reshape(1,100), sentence_vectors[j].reshape(1,100))[0,0]


# Converting the matrix into a graph
# Scores is a dictionary, which contains the rank of each sentence
# Higher score(rank) means the sentence is more important

nx_graph = nx.from_numpy_array(sim_mat)
scores = nx.pagerank(nx_graph)

# Ranking the sentences in DESCENDING order
# This will keep only the most important sentences
# Keys and values are used to order the sentences later

ranked_sentences = sorted(([scores[i],s] for i,s in enumerate(sentences)), reverse=True)
keys = list(scores.keys())
values = list(scores.values())

# Short summary (1/4th the size of the original text)
# Ordered sentences contain the most important sentences in the order at which it was obtained

s_n = int((1/4) * len(ranked_sentences))
short_ranked_sentences = ranked_sentences[: s_n + 1]
short_ordered_sentences = []
for i, j in short_ranked_sentences:
    ind = values.index(i)
    short_ordered_sentences.append([ind, j])
short_ordered_sentences = sorted(short_ordered_sentences, key=lambda x: x[0])
f = open("TextSumShort.txt", 'w')
for i in range(len(short_ordered_sentences)):
    f.write(short_ordered_sentences[i][1])
    f.write("\n")
f.close()

# Split extension and name
s3_fname = localname.split('.')

# Upload summary to S3 bucket
try:
	s3_client.upload_file("TextSumShort.txt",'nh-lecture-files200138-dev',"public/"+key_list[1]+"/"+s3_fname[0]+"-Short.txt")
except ClientError as e:
	print(e)
	raise e

# Long summary (3/4th the size of the original text))

l_n = int((3/4) * len(ranked_sentences))
long_ranked_sentences = ranked_sentences[: l_n + 1]
long_ordered_sentences = []
for i, j in long_ranked_sentences:
    ind = values.index(i)
    long_ordered_sentences.append([ind, j])
long_ordered_sentences = sorted(long_ordered_sentences, key=lambda x: x[0])
f = open("TextSumLong.txt", 'w')
for i in range(len(long_ordered_sentences)):
    f.write(long_ordered_sentences[i][1])
    f.write("\n")
f.close()

# Upload summary to S3 bucket
try:
	s3_client.upload_file("TextSumLong.txt",'nh-lecture-files200138-dev',"public/"+key_list[1]+"/"+s3_fname[0]+"-Long.txt")
except ClientError as e:
	print(e)
	raise e

# Medium summary (1/2 the size of the original text)

m_n = int((1/2) * len(ranked_sentences))
medium_ranked_sentences = ranked_sentences[: m_n + 1]
medium_ordered_sentences = []
for i, j in medium_ranked_sentences:
    ind = values.index(i)
    medium_ordered_sentences.append([ind, j])
medium_ordered_sentences = sorted(medium_ordered_sentences, key=lambda x: x[0])
f = open("TextSumMed.txt", 'w')
for i in range(len(medium_ordered_sentences)):
    f.write(medium_ordered_sentences[i][1])
    f.write("\n")
f.close()

# Upload summary to S3 bucket
try:
	s3_client.upload_file("TextSumMed.txt",'nh-lecture-files200138-dev',"public/"+key_list[1]+"/"+s3_fname[0]+"-Med.txt")
except ClientError as e:
	print(e)
	raise e

os.remove("TextSumShort.txt")
os.remove("TextSumMed.txt")
os.remove("TextSumLong.txt")
os.remove(localname)
