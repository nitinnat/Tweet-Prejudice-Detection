# -*- coding: utf-8 -*-
"""
Created on Thu May 10 16:07:18 2018

@author: Nitin
"""

import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
np.random.seed(42) #Set seed
filepath_not_NER = "./FeatureVectorNotNER.csv"
filepath_NER = "./FeatureVectorNER.csv"
output_file_train_NER = "train-Strat-NER-{}.csv"
output_file_test_NER = "test-Strat-NER-{}.csv"
output_file_train_not_NER = "train-Strat-NotNER-{}.csv"
output_file_test_not_NER = "test-Strat-NotNER-{}.csv"
#90-10, 80-20,70-30 and 60-40. 
def read_data(filepath):
    df = pd.read_csv(filepath,header = 0)
    return df
    
def make_split(df,train_split):
    train, test = train_test_split(df, test_size=1-train_split, random_state=0)
    return train,test
if __name__ == "__main__":
    X_not_NER = read_data(filepath_not_NER)
    X_NER = read_data(filepath_NER)
    for split in [0.9,0.8,0.7,0.6]:
        X_train, X_test = make_split(X_not_NER,split)
        print(X_train.shape, X_test.shape)
        X_train.to_csv(output_file_train_not_NER.format(int(split*100)), index = None, header = None)
        X_test.to_csv(output_file_test_not_NER.format(int(split*100)), index = None,header = None)
        
        #NER
        X_train, X_test = make_split(X_NER,split)
        print(X_train.shape, X_test.shape)
        X_train.to_csv(output_file_train_NER.format(int(split*100)), index = None, header = None)
        X_test.to_csv(output_file_test_NER.format(int(split*100)), index = None,header = None)
        
        