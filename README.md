# NERanker
Contains the source code for CSC 583 project: "Ranking Search Engine Results Based on Named Entities in Queries".

Instructions for running the Lucene component of the code to generate Top-20 candidate answers for each Jeopardy question:
* The index (available here:) must be pasted inside the `LucenePreprocessor/src/main/resources` folder. Or it can be built from scratch on the Jeopardy dataset, available in the `wiki_dataset` folder by running `IndexTrainer.java` inside `LucenePreprocessor/src/main/java/`.
* Run `QueryEngine.java` to create a `questions` folder which will contain sub-folders of the format Qi where i = [1,2..100], containing the titles of the top-20 most relevant documents for each question based on Lucene  `LMDirichletSimilarity`.

Analysis: 
