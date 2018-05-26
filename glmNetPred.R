#Load the glmnet Regression package
#bayesQR
library("glmnet")
#Use ROCR for precision recall metrics
library("ROCR")
library("pROC")
#Load glmpath
#library(glmpath)
setwd('C:/Users/Nitin/Documents/Projects/Haimonti - Tweet Prejudice/R-code/R-code')
# Read the train and test data
trData<-read.csv(file="train-Strat-NotNER-90.csv",header=FALSE,sep=",")
teData<-read.csv(file="test-Strat-NotNER-90.csv",header=FALSE,sep=",")
rTr<-nrow(trData)
rTe<-nrow(teData)
cTr<-ncol(trData)
cTe<-ncol(teData)

# Get the features
trDataSub<-as.matrix(trData[,2:cTr])
teDataSub<-as.matrix(teData[,2:cTe])

#trDataSub<-trData[,1:cTr-1]
#teDataSub<-teData[,1:cTe-1]
#Get the labels
#We do not include column 2 which is target negation
trCl<-as.matrix(trData[,1])
teCl<-as.matrix(teData[,1])

#trCl<-trData[,cTr]
#teCl<-teData[,cTe]

#=======================
#Build logistic regression model (No regularization)
#fit=glm(`trCl`~.,data=trDataSub, family=binomial(link="logit"))
#summary(fit)
#coef(fit)
#Performance on test data
#yHatNoReg=predict(fit, teDataSub,type="response")
#predNoReg<- prediction(yHatNoReg, teCl)
#dev.new()
#Find true positive rate and false positive rate
#perfNoReg <- performance(predNoReg,"tpr","fpr")
#plot(perfNoReg)
#dev.new()
#pfNoReg<-performance(yHatNoReg,"auc")
#plot(pfNoReg)
#Build logistic regression model with L1 regularization
#Uses glmpath
#fit1=cv.glmpath(trDataSub,trCl, family="binomial",type="response")
#Understanding the regression coefficients
#coef(fit1)
#which(coef(fit1)[1:cTr+1]>0)
#which(coef(fit1)[1:cTr+1]<0)
#Test on unseen data
#yHat1=predict(fit1,teDataSub,type="response"ubl)

#Estimate accuracy, precision, recall, F1-score
#pred1 <- prediction( yHat1, teCl)
#Generate a ROC curve
#dev.new()
#pf1 <- performance(pred1,"tpr","fpr")
#plot(pf1)
#Generate a precision-recall curve
#dev.new()
#pf2 <- performance(pred1,"prec","rec")
#plot(pf2)
#=========================



#Build a lasso model and a ridge model 



#mseLogit = mean((teData[,cTe] - yhatLasso[,99])^2)
#mseLasso = mean((teData[,cTe] - yhatLasso[,99])^2)
#mseRidge <- mean((teData[,cTe] - yhatRidge[,99])^2)
#mseElNet <- mean((teData[,cTe] - yhatElNet[,99])^2)


#Build a logistic regression model
fit.logit <-glmnet(trDataSub, trCl, family="binomial", lambda=0)
yhatLogit <- predict(fit.logit, s=fit.logit$lambda.min, newx=teDataSub,type = "class")
#Estimate accuracy, precision, recall and F1 score from Logit model.
cmLogit = as.matrix(table(Actual = teData[,cTe], Predicted = yhatLogit))
nLogit = sum(cmLogit)
ncLogit = nrow(cmLogit)
diagLogit = diag(cmLogit)
rowsumsLogit = apply(cmLogit, 1, sum)
colsumsLogit = apply(cmLogit, 2, sum)
pLogit = rowsumsLogit / nLogit
qLogit = colsumsLogit / nLogit
accLogit = sum(diagLogit) / nLogit 
precLogit = diagLogit / colsumsLogit 
recLogit = diagLogit / rowsumsLogit 
f1Logit = 2 * precLogit * recLogit / (precLogit + recLogit) 
predLogit <- prediction( as.matrix(type.convert(yhatLogit)), teData[,cTe])
#Generate a ROC curve
#dev.new()
#perfAUCLogit <- performance(predLogit,"tpr","fpr")
#lty: line type and lwd: line width
#plot(perfAUCLogit,type="l",lty=2,lwd=1)
#pfAUCLogit<-performance(predLogit,"auc")
roc_obj_logit <- roc(teData[,cTe], type.convert(yhatLogit))
aucScoreLogit <- auc(roc_obj_logit)

############################################################################

#Estimate accuracy, precision, recall and F1 score from Lasso model.
fit.lasso <- glmnet(trDataSub, trCl, family="binomial", alpha=1)
yhatLasso <- predict(fit.lasso, s=fit.lasso$lambda.min, newx=teDataSub,type = "class")
cmLasso = as.matrix(table(Actual = teData[,cTe], Predicted = yhatLasso[,100]))
nLasso = sum(cmLasso)
ncLasso = nrow(cmLasso)
diagLasso = diag(cmLasso)
rowsumsLasso = apply(cmLasso, 1, sum)
colsumsLasso = apply(cmLasso, 2, sum)
pLasso = rowsumsLasso / nLasso
qLasso = colsumsLasso / nLasso
accLasso = sum(diagLasso) / nLasso 
precLasso = diagLasso / colsumsLasso 
recLasso = diagLasso / rowsumsLasso 
f1Lasso = 2 * precLasso * recLasso / (precLasso + recLasso) 
predLasso <- prediction( as.matrix(type.convert(yhatLasso)[,100]), teData[,cTe])
#Generate a ROC curve
#dev.new()
#perfAUCLasso <- performance(predLasso,"tpr","fpr")
#lty: line type and lwd: line width
#plot(perfAUCLasso,type="l",lty=2,lwd=1)
#pfAUCLasso<-performance(predLasso,"auc")
roc_obj_lasso <- roc(teData[,cTe], type.convert(yhatLasso[,100]))
aucScoreLasso <- auc(roc_obj_lasso)


##################################################################################
fit.ridge <- glmnet(trDataSub, trCl, family="binomial", alpha=0)
yhatRidge <- predict(fit.ridge, s=fit.ridge$lambda.min, newx=teDataSub,type = "class")
#Estimate accuracy, precision, recall and F1 score from Ridge model.
for (i in 1:401)
{
if(yhatRidge[i,100]>-3.00)
yhatRidge[i,100]=1
else yhatRidge[i,100]=0
}
cmRidge = as.matrix(table(Actual = teData[,cTe], Predicted = yhatRidge[,100]))
nRidge = sum(cmRidge)
ncRidge = nrow(cmRidge)
diagRidge = diag(cmRidge)
rowsumsRidge = apply(cmRidge, 1, sum)
colsumsRidge = apply(cmRidge, 2, sum)
pRidge = rowsumsRidge / nRidge
qRidge = colsumsRidge / nRidge
accRidge = sum(diagRidge) / nRidge 
precRidge = diagRidge / colsumsRidge 
recRidge = diagRidge / rowsumsRidge
f1Ridge = 2 * precRidge * recRidge / (precRidge + recRidge) 
predRidge <- prediction( as.matrix(type.convert(yhatLasso)[,100]), teData[,cTe])
#Generate a ROC curve
#perfAUCRidge <- performance(predRidge,"tpr","fpr")
#par(new = TRUE)
#plot(perfAUCRidge,type="l",lty=4,lwd=1)
#pfAUCRidge<-performance(predRidge,"auc")
roc_obj_ridge <- roc(teData[,cTe], type.convert(yhatRidge[,100]))
aucScoreRidge <- auc(roc_obj_ridge)

#####################################################################################3


fit.elnet <- glmnet(trDataSub, trCl, family="binomial", alpha=0.8)
yhatElNet <- predict(fit.elnet, s=fit.elnet$lambda.min, newx=teDataSub,type = "class")
#Estimate accuracy, precision, recall and F1 score from Elastic Net model.
cmElNet = as.matrix(table(Actual = teData[,cTe], Predicted = yhatElNet[,100]))
nElNet = sum(cmElNet)
ncElNet = nrow(cmElNet)
diagElNet = diag(cmElNet)
rowsumsElNet = apply(cmElNet, 1, sum)
colsumsElNet = apply(cmElNet, 2, sum)
pElNet = rowsumsElNet / nElNet
qElNet = colsumsElNet / nElNet
accElNet = sum(diagElNet) / nElNet
precElNet = diagElNet / colsumsElNet 
recElNet = diagElNet / rowsumsElNet
f1ElNet = 2 * precElNet * recElNet / (precElNet + recElNet) 
predElNet <- prediction( as.matrix(type.convert(yhatElNet)[,100]), teData[,cTe])
#Generate a ROC curve
#perfAUCElNet <- performance(predElNet,"tpr","fpr")
#par(new = TRUE)
#plot(perfAUCElNet,type="l",lty=1,lwd=1)
#pfAUCElNet<-performance(predElNet,"auc")
roc_obj_elnet <- roc(teData[,cTe], type.convert(yhatElNet[,100]))
aucScoreElnet <- auc(roc_obj_elnet)

###########################################################33


df <- data.frame(precLogit,recLogit, f1Logit, accLogit, 
                 precLasso, recLasso, f1Lasso, accLasso,
                 precRidge, recRidge, f1Ridge,accRidge, 
                 precElNet, recElNet, f1ElNet,accElNet) 

write.table(df, "./rt_results_8000.csv", sep=",")
###############################################################33
#Estimate accuracy, precision, recall, F1-score
predLasso <- prediction( as.matrix(type.convert(yhatLasso)[,100]), teData[,cTe])
#Generate a ROC curve
dev.new()
perfAUCLasso <- performance(predLasso,"tpr","fpr")
#lty: line type and lwd: line width
plot(perfAUCLasso,type="l",lty=2,lwd=1)
pfAUCLasso<-performance(predLasso,"auc")

predRidge <- prediction( as.matrix(type.convert(yhatLasso)[,100]), teData[,cTe])
#Generate a ROC curve
perfAUCRidge <- performance(predRidge,"tpr","fpr")
par(new = TRUE)
plot(perfAUCRidge,type="l",lty=4,lwd=1)
pfAUCRidge<-performance(predRidge,"auc")

predElNet <- prediction( as.matrix(type.convert(yhatElNet)[,100]), teData[,cTe])
#Generate a ROC curve
perfAUCElNet <- performance(predElNet,"tpr","fpr")
par(new = TRUE)
plot(perfAUCElNet,type="l",lty=1,lwd=1)
pfAUCElNet<-performance(predElNet,"auc")

yhatLogReg<-read.csv(file="predLogReg-90.csv",header=FALSE)
predLogReg <- prediction(as.matrix(yhatLogReg) , teData[,cTe])
#Generate a ROC curve
perfAUCLogReg <- performance(predLogReg,"tpr","fpr")
par(new = TRUE)
plot(perfAUCLogReg,type="l",lty=3,lwd=1)
legend("topright", inset=.05, title="Method",
       c("Lasso","Ridge","Elastic Net", "Log Reg"), lty=c(2,4,1,3), horiz=FALSE)

pfAUCLogReg<-performance(predLogReg,"auc")



##################################################33
#Build logistic regression model with regularization
#Uses Elastic net - glmnet
fit1=cv.glmnet(trDataSub,trCl, family="binomial",type.measure="auc")
for (i in 1:9) 
{
    assign(paste("fit", i, sep=""), cv.glmnet(trDataSub,trCl,family="binomial",alpha=i/10))
}


#Understanding the regression coefficients
#coef(fit1)
#which(coef(fit1)[1:cTr+1]>0)
#which(coef(fit1)[1:cTr+1]<0)

#Test on unseen data
#yHat=predict.cv.glmnet(fit1,teDataSub, type="response")
#print(fit1)
#yhat0 <- predict(fit0, s=fit0$lambda.min, newx=teDataSub,type.measure = "class")
#yhat1 <- predict(fit1, s=fit1$lambda.min, newx=teDataSub)
#yhat2 <- predict(fit2, s=fit2$lambda.min, newx=teDataSub)
#yhat3 <- predict(fit3, s=fit3$lambda.min, newx=teDataSub)
#yhat4 <- predict(fit4, s=fit4$lambda.min, newx=teDataSub)
#yhat5 <- predict(fit5, s=fit5$lambda.min, newx=teDataSub)
#yhat6 <- predict(fit6, s=fit6$lambda.min, newx=teDataSub)
#yhat7 <- predict(fit7, s=fit7$lambda.min, newx=teDataSub)
#yhat8 <- predict(fit8, s=fit8$lambda.min, newx=teDataSub)
#yhat9 <- predict(fit9, s=fit9$lambda.min, newx=teDataSub)
#yhat10 <- predict(fit10, s=fit10$lambda.min, newx=teDataSub)

#mse0 <- mean((teData[,cTe] - yhat0)^2)
#mse1 <- mean((teData[,cTe] - yhat1)^2)
#mse2 <- mean((teData[,cTe] - yhat2)^2)
#mse3 <- mean((teData[,cTe] - yhat3)^2)
#mse4 <- mean((teData[,cTe] - yhat4)^2)
#mse5 <- mean((teData[,cTe] - yhat5)^2)
#mse6 <- mean((teData[,cTe] - yhat6)^2)
#mse7 <- mean((teData[,cTe] - yhat7)^2)
#mse8 <- mean((teData[,cTe] - yhat8)^2)
#mse9 <- mean((teData[,cTe]- yhat9)^2)
#mse10 <- mean((teData[,cTe] - yhat10)^2)

#Estimate accuracy, precision, recall, F1-score
predLasso <- prediction( as.matrix(yhatLasso[,100]), teData[,cTe])
#Generate a ROC curve
dev.new()
perfAUCLasso <- performance(predLasso,"tpr","fpr")
#lty: line type and lwd: line width
plot(perfAUCLasso,type="l",lty=2,lwd=1)
pfAUCLasso<-performance(predLasso,"auc")

predRidge <- prediction( as.matrix(type.convert(yhatLasso)[,100]), teData[,cTe])
#Generate a ROC curve
perfAUCRidge <- performance(predRidge,"tpr","fpr")
par(new = TRUE)
plot(perfAUCRidge,type="l",lty=4,lwd=1)
pfAUCRidge<-performance(predRidge,"auc")

predElNet <- prediction( as.matrix(yhatElNet[,100]), teData[,cTe])
#Generate a ROC curve
perfAUCElNet <- performance(predElNet,"tpr","fpr")
par(new = TRUE)
plot(perfAUCElNet,type="l",lty=1,lwd=1)
pfAUCElNet<-performance(predElNet,"auc")

yhatLogReg<-read.csv(file="predLogReg-90.csv",header=FALSE)
predLogReg <- prediction(as.matrix(yhatLogReg) , teData[,cTe])
#Generate a ROC curve
perfAUCLogReg <- performance(predLogReg,"tpr","fpr")
par(new = TRUE)
plot(perfAUCLogReg,type="l",lty=3,lwd=1)
legend("topright", inset=.05, title="Method",
  	c("Lasso","Ridge","Elastic Net", "Log Reg"), lty=c(2,4,1,3), horiz=FALSE)

pfAUCLogReg<-performance(predLogReg,"auc")


  	
#Generate a precision-recall curve
#dev.new()
#perf2 <- performance(pred,"prec","rec")
#plot(perf2)

#pfAccLasso<-performance(predLasso,"acc")
#pfPreLasso<-performance(predLasso,"prec")
#pfRecLasso<-performance(predLasso,"rec")
#pfAUCLasso<-performance(predLasso,"auc")
#pfFMeasLasso<-performance(predLasso,"f")
