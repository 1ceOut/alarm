import sys
import json
from sklearn.decomposition import TruncatedSVD
from scipy.sparse.linalg import svds
import mysql.connector
import pandas as pd
import numpy as np

# 사용자 ID를 인자로 받음
user_id = sys.argv[1]

# MySQL 데이터베이스 접속 설정
db_config = {
    'user': 'study',
    'password': 'bitcamp!@#123',
    'host': 'db-n9jh9-kr.vpc-pub-cdb.ntruss.com',
    'database': 'studydb',
    'port': 3306
}

# MySQL 데이터베이스에서 comment 테이블의 데이터 가져오기
def get_data_from_db():
    try:
        # 데이터베이스 연결
        connection = mysql.connector.connect(**db_config)

        # comment 테이블에서 user_id, rate, posting_id 가져오기
        query = """
        SELECT user_id, rate, posting_id 
        FROM comment
        """
        data = pd.read_sql(query, connection)

        return data
    finally:
        connection.close()

# 사용자-레시피 평점 매트릭스 생성 및 SVD 수행
def matrix_factorization():
    # 데이터 가져오기
    rating_data = get_data_from_db()
    #print(rating_data)

    # 중복된 user_id와 posting_id가 있을 경우 나중에 나오는 데이터로 유지
    rating_data = rating_data.drop_duplicates(subset=['user_id', 'posting_id'], keep='last')

    # 사용자-레시피 평점 피벗 테이블 생성
    user_recipe_ratings = rating_data.pivot(
        index='user_id',
        columns='posting_id',
        values='rate'
    ).fillna(0)  # 평점이 없는 부분은 0으로 채움

    # 매트릭스를 numpy 행렬로 변환
    matrix = user_recipe_ratings.to_numpy()

    # 사용자의 평균 평점 계산
    user_ratings_mean = np.mean(matrix, axis=1)

    # 사용자 평균 평점을 뺀 매트릭스
    matrix_user_mean = matrix - user_ratings_mean.reshape(-1, 1)

    # SVD 수행
    U, sigma, Vt = svds(matrix_user_mean, k=12)
    sigma = np.diag(sigma)

    # SVD 복원된 사용자-레시피 평점 예측
    svd_user_predicted_ratings = np.dot(np.dot(U, sigma), Vt) + user_ratings_mean.reshape(-1, 1)

    # 예측 평점을 데이터프레임으로 변환
    df_svd_preds = pd.DataFrame(svd_user_predicted_ratings, columns=user_recipe_ratings.columns, index=user_recipe_ratings.index)

    return df_svd_preds, user_recipe_ratings, rating_data

# 레시피 추천 함수 (posting_id만 반환)
def recommend_posting_ids(df_svd_preds, user_id, rating_data, num_recommendations=5):
    # 사용자 예측 평점 정렬
    sorted_user_predictions = df_svd_preds.loc[user_id].sort_values(ascending=False)

    # 사용자가 이미 평가한 레시피 가져오기
    user_data = rating_data[rating_data.user_id == user_id]

    # 이미 평가한 posting_id 목록
    already_rated_ids = user_data['posting_id'].tolist()

    # 추천 리스트에서 사용자가 이미 평가한 레시피는 제외
    recommendations = sorted_user_predictions.index[~sorted_user_predictions.index.isin(already_rated_ids)]

    # 추천된 posting_id 반환 (상위 num_recommendations개)
    return already_rated_ids, recommendations[:num_recommendations]

# 데이터 가져오기 및 추천 시스템 수행
df_svd_preds, user_recipe_ratings, rating_data = matrix_factorization()

# 사용자 ID에 따라 추천 수행
#user_id = input("추천을 받을 사용자 ID를 입력하세요: ")
already_rated, recommended_posting_ids = recommend_posting_ids(df_svd_preds, user_id, rating_data, 5)

#print(f"User {user_id} has already rated the following posting IDs:")
#print(already_rated)

#print(f"\nTop recommended posting IDs for user {user_id}:")
#print(recommended_posting_ids.tolist())
print(json.dumps(recommended_posting_ids.tolist()))
