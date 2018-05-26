# -*- coding: utf-8 -*-
"""
Created on Thu May 24 23:08:26 2018

@author: Nitin
"""

import glmnet_python
from glmnet import glmnet
from glmnetPredict import glmnetPredict
from sklearn import linear_model
from sklearn.metrics import f1_score, accuracy_score, confusion_matrix, roc_auc_score, precision_score, recall_score
import numpy as np
import pandas_ml as pdml
import pandas as pd
import scipy


def balanced_sample(data,target):

    df = pdml.ModelFrame(data,
                      target=target)
    sampler = df.imbalance.under_sampling.ClusterCentroids()
    sampled = df.fit_sample(sampler)
    print(sampled.target.value_counts())
    return sampled

def ml_model(train_file, test_file,mtype = "logreg",balance = False):
    train = pd.read_csv(train_file,header = None)
    test = pd.read_csv(test_file,header = None)
    
    
    train= np.array(train)
    test = np.array(test)
    X_train = np.array(train[:,1:],dtype="float64")
    X_test = np.array(test[:,1:],dtype="float64")
    y_train = np.array(train[:,0],dtype="float64")
    y_test = np.array(test[:,0],dtype="float64")

    
    #print("X_train:{}, y_train: {}, X_test: {}, y_test: {}".format(X_train.shape, 
    #      y_train.shape, X_test.shape, y_test.shape))
    if mtype == "logreg":
        fit = glmnet(x = X_train, y = y_train, family = 'binomial')

    elif mtype == "lasso":
    	fit = glmnet(x = X_train, y = y_train, family = 'binomial',alpha=1)

    elif mtype == "ridge":
    	fit = glmnet(x = X_train, y = y_train, family = 'binomial',alpha=0)
    elif mtype == "elnet":
    	fit = glmnet(x = X_train, y = y_train, family = 'binomial',alpha=0.8)
    
    y_pred = glmnetPredict(fit, newx = X_test,ptype = "class",s = scipy.array([0.0]))
    print(y_test.shape, y_pred.shape)
    f1score = f1_score(y_test,y_pred)
    acc = accuracy_score(y_test,y_pred)
    cm = confusion_matrix(y_test,y_pred)
    auc = roc_auc_score(y_test, y_pred)
    precision = precision_score(y_test,y_pred)
    recall = recall_score(y_test, y_pred)
    print("Accuracy: {} and f1score: {}.".format(acc, f1score))
    print("Confusion matrix: \n {}".format(cm))
    return f1score, acc, str(cm), auc, precision, recall


    
if __name__ == "__main__":
    train_file_NER = 'train-Strat-NER-{}.csv'
    test_file_NER = 'test-Strat-NER-{}.csv'
    train_file_not_NER = 'train-Strat-NotNER-{}.csv'
    test_file_not_NER = 'test-Strat-NotNER-{}.csv'
    
    """
    Output into a dataframe
    """

    namesplit, model_names, f1_scores, accuracies,cms,aucs, precisions, recalls = [],[],[],[],[],[],[],[]
    for s in [90,80,70,60]:
        
        #Logistic Regression
        print("Normal logistic regression, lambda = 0.0")
        #NER
        print("Results on NER with split {}".format(s))
        namesplit.append("NER with split {}/{}".format(s,100-s))
        f1, acc, cm,auc,precision,recall = ml_model(train_file_NER.format(s),test_file_NER.format(s),mtype="logreg",balance=True)
        model_names.append("Logistic Regression")
        f1_scores.append(np.round(f1*100,2))
        accuracies.append(np.round(acc*100,2))
        cms.append(cm)
        aucs.append(auc)
        precisions.append(precision)
        recalls.append(recall)
        
        #Non-NER
        print("Results on non-NER with split {}".format(s))
        namesplit.append("GroupIndividual with split {}/{}".format(s,100-s))
        f1, acc,cm,auc,precision,recall = ml_model(train_file_not_NER.format(s),test_file_not_NER.format(s),mtype="logreg",balance =True)
        model_names.append("Logistic Regression")
        f1_scores.append(np.round(f1*100,2))
        accuracies.append(np.round(acc*100,2))
        cms.append(cm)
        aucs.append(auc)
        precisions.append(precision)
        recalls.append(recall)
        
        #Lasso
        print("Lasso logistic regression, alpha = 1")
        #NER
        print("Results on NER with split {}".format(s))
        namesplit.append("NER with split {}/{}".format(s,100-s))
        f1, acc, cm,auc,precision,recall = ml_model(train_file_NER.format(s),test_file_NER.format(s),mtype = "lasso")
        model_names.append("Lasso")
        f1_scores.append(np.round(f1*100,2))
        accuracies.append(np.round(acc*100,2))
        cms.append(cm)
        aucs.append(auc)
        precisions.append(precision)
        recalls.append(recall)
                       
        #Non-NER
        print("Results on non-NER with split {}".format(s))
        namesplit.append("GroupIndividual with split {}/{}".format(s,100-s))
        f1, acc,cm,auc,precision,recall = ml_model(train_file_not_NER.format(s),test_file_not_NER.format(s),mtype = "lasso")
        model_names.append("Lasso")
        f1_scores.append(np.round(f1*100,2))
        accuracies.append(np.round(acc*100,2))
        cms.append(cm)
        aucs.append(auc)
        precisions.append(precision)
        recalls.append(recall)
        
        #Ridge
        print("Ridge logistic regression, alpha = 0")
        #NER
        print("Results on NER with split {}".format(s))
        namesplit.append("NER with split {}/{}".format(s,100-s))
        f1, acc, cm,auc,precision,recall = ml_model(train_file_NER.format(s),test_file_NER.format(s),mtype = "ridge")
        model_names.append("Ridge")
        f1_scores.append(np.round(f1*100,2))
        accuracies.append(np.round(acc*100,2))
        cms.append(cm)
        aucs.append(auc)
        precisions.append(precision)
        recalls.append(recall)
                       
        #Non-NER
        print("Results on non-NER with split {}".format(s))
        namesplit.append("GroupIndividual with split {}/{}".format(s,100-s))
        f1, acc,cm,auc,precision,recall = ml_model(train_file_not_NER.format(s),test_file_not_NER.format(s),mtype = "ridge")
        model_names.append("Ridge")
        f1_scores.append(np.round(f1*100,2))
        accuracies.append(np.round(acc*100,2))
        cms.append(cm)
        aucs.append(auc)
        precisions.append(precision)
        recalls.append(recall)
        
        #Elastic Net
        print("Elastic Net logistic regression, alpha = 0.8")
        #NER
        print("Results on NER with split {}".format(s))
        namesplit.append("NER with split {}/{}".format(s,100-s))
        f1, acc, cm,auc,precision,recall = ml_model(train_file_NER.format(s),test_file_NER.format(s),mtype = "elnet")
        model_names.append("Elnet")
        f1_scores.append(np.round(f1*100,2))
        accuracies.append(np.round(acc*100,2))
        cms.append(cm)
        aucs.append(auc)
        precisions.append(precision)
        recalls.append(recall)
                       
        #Non-NER
        print("Results on non-NER with split {}".format(s))
        namesplit.append("GroupIndividual with split {}/{}".format(s,100-s))
        f1, acc,cm,auc,precision,recall = ml_model(train_file_not_NER.format(s),test_file_not_NER.format(s),mtype = "elnet")
        model_names.append("Elnet")
        f1_scores.append(np.round(f1*100,2))
        accuracies.append(np.round(acc*100,2))
        cms.append(cm)
        aucs.append(auc)
        precisions.append(precision)
        recalls.append(recall)
        
    df = pd.DataFrame(data={"NameAndSplit": namesplit, 
                            "ModelName":model_names,
                            "F1Score":f1_scores,
                            "Accuracy":accuracies,
                            "Precision":precisions,
                            "Recall":recalls,
                            "AUCScore":aucs,
                            "ConfusionMatrix":cms,
                            })
    df.to_csv("TweetResults.csv",columns = ["NameAndSplit", "ModelName","F1Score",
                                            "Accuracy", "Precision","Recall","AUCScore",
                                            "ConfusionMatrix"],index = None) 
    


