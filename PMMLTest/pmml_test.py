# coding:utf-8
import sklearn, sklearn.externals.joblib, sklearn_pandas, sklearn2pmml
import sys
reload(sys)
sys.setdefaultencoding( "utf-8" )
from sklearn2pmml import PMMLPipeline
from sklearn.datasets import load_iris
from sklearn import tree


#
from sklearn2pmml import sklearn2pmml
from sklearn.feature_extraction import dict_vectorizer
from sklearn.feature_extraction import DictVectorizer
from sklearn.tree import DecisionTreeClassifier
from sklearn.linear_model import LogisticRegression

from sklearn.ensemble import GradientBoostingClassifier


from sklearn.pipeline import Pipeline

from sklearn_pandas import DataFrameMapper

import pandas as pd
import numpy as np
pd.set_option('display.max_columns', None)
#sklearn2pmml(pipeline, "DecisionTreeIris.pmml", with_repr=True)
from sklearn.preprocessing import *
heart_data = pd.read_csv("heart.csv")
print heart_data



#LabelBinarizer()
#用Mapper定义特征工程
mapper = DataFrameMapper([
    (['sbp'], MinMaxScaler()),
    (['tobacco'], MinMaxScaler()),
    ('ldl', None),
    ('adiposity', None),
    ('famhist',LabelBinarizer() ),
    ('typea', None),
    ('obesity', None),
    ('alcohol', None),
#(['age'], FunctionTransformer(np.log))
(['age'], Imputer(missing_values= -1, strategy="mean"))
],
df_out=True)




'''

iris_pipeline = PMMLPipeline([
        ("mapper", DataFrameMapper([
            (["Sepal.Length", "Sepal.Width", "Petal.Length", "Petal.Width"], [ContinuousDomain(), Imputer()])])),
        ("pca", PCA(n_components = 3)),
        ("selector", SelectKBest(k = 2)),
        ("classifier", GradientBoostingClassifier())])


mapper2 = DataFrameMapper([
    ('sbp', None),
    ('tobacco', None),
    ('ldl', None),
    ('adiposity', None),
    (['famhist'],None ),
    ('typea', None),
    ('obesity', None),
    ('alcohol', None),
    (['age'], FunctionTransformer(np.log))
],
df_out=True)
'''

#vvv=mapper.fit_transform(heart_data[heart_data.columns.difference(["chd"])])
#print vvv
'''
union = FeatureUnion([
  ("first", mapper),
  ("second", mapper2)
])
'''

#tt= mapper2.fit_transform(heart_data[heart_data.columns.difference(["chd"])])
#print tt['famhist']

#用pipeline定义使用的模型，特征工程等
pipeline = PMMLPipeline([
    ('mapper', mapper),
   ("classifier",GradientBoostingClassifier())
#("classifier",LogisticRegression())
])

#vv=pipeline.fit(heart_data[heart_data.columns.difference(["chd"])])

vv=pipeline.fit(heart_data[heart_data.columns.difference(["chd"])] ,heart_data["chd"] )
print vv



#print pipeline.transform(heart_data[heart_data.columns.difference(["chd"])])

print pipeline.predict(heart_data[heart_data.columns.difference(["chd"])])


sklearn2pmml(pipeline, "lrHeart.pmml", with_repr = True,debug=True)
