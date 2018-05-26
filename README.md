# Tweet-Prejudice-Detection

<br>
FeatureVectorNER.csv is the feature file with NER features.
Here are the modifications made to this file:
<ol>
	<li>2nd column, i.e. the negated target column is removed MANUALLY.</li>
<li>Group column is replaced by the NER-features obtained by the Java code.
	"Individual"  column is then removed MANUALLY.</li>
	

</ol>

</br>

<br>
FeatureVectorNotNER.csv is the original features file with Group and Individual features.
Here are the modifications made to this file:
<ol>
	<li>2nd column, i.e. the negated target column is removed MANUALLY.
	</li>


</ol>

</br>

<br>
The other files in the format train-Strat-NER-{}.csv or test-Strat-NER-{}.csv
represent the train and test sets for a particular split for the features with NER.

This is similar in the case for NotNER feature files.
</br>