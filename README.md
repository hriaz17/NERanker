# NERanker
Contains the source code for CSC 583 project: "Ranking Search Engine Results Based on Named Entities in Queries".

Instructions for running the Lucene component of the code to generate Top-20 candidate answers for each Jeopardy question:
* The index (available here:) must be pasted inside the `LucenePreprocessor/src/main/resources` folder. Or it can be built from scratch on the Jeopardy dataset (which must be downloaded from [here](https://www.dropbox.com/s/nzlb96ejt3lhd7g/wiki-subset-20140602.tar.gz?dl=0)) and extracted into a folder called `wiki_dataset`, which should be followed up by running `IndexTrainer.java` inside `LucenePreprocessor/src/main/java/`.
* Run `QueryEngine.java` to create a `questions` folder which will contain sub-folders of the format Qi where i = [1,2..100], containing the titles of the top-20 most relevant documents for each question based on Lucene  `LMDirichletSimilarity`.

Instructions for finetuning NER, generating NER labels for jeopardy questions and candidates, EntityTFIDF implementation and evaluation:
* For loading the SSL NER models, and fine-tuning on the tweetner7 dataset, run the notebook titled `tweetner-dataset-preprocessing+fully-supervised-ner-example.ipynb`. This notebook also evaluates of TFIDF and Lucene based on MRR.
* For loading the finetuned model, generating NER labels for every question in Jeopardy, as well as for each token in each of the 2000 documents (obtained after Lucene filtering), run the notebook titled `jeopardy_NER_tagging.ipynb`.
