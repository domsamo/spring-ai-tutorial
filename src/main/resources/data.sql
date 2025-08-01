-- Authors 테이블에 데이터 삽입
INSERT INTO Authors (name) VALUES
('허정준'),
('박해선'),
('오렐리앙 제롱'),
('조민정'),
('사이토 고키'),
('김진중'),
('오노 사토시'),
('유호석');

-- Publishers 테이블에 데이터 삽입
INSERT INTO Publishers (name) VALUES
('책만'),
('한빛미디어'),
('리코멘드'),
('제이펍');

-- Books 테이블에 데이터 삽입
INSERT INTO Books (title, author_ref, publisher_ref) VALUES
('LLM을 활용한 실전 AI 애플리케이션 개발', 1, 1),
('혼자 공부하는 머신러닝+딥러닝', 2, 2),
('핸즈온 머신러닝', 3, 2),
('챗GPT 제대로 써먹기', 4, 2),
('밑바닥부터 시작하는 딥러닝', 5, 2),
('최고의 프롬프트 엔지니어링 강의', 6, 3),
('소프트웨어 개발에 ChatGPT 사용하기', 7, 4),
('제가 만든 GPT는 당신이 만든 GPT와 전혀 다릅니다', 8, 3);

-- Directors (감독 데이터)
INSERT INTO Directors (id, name, birth_year) VALUES
(1, '봉준호', 1969),
(2, '박찬욱', 1963),
(3, '김한민', 1969),
(4, '연상호', 1978),
(5, '이창동', 1954);

-- Actors (배우 데이터)
INSERT INTO Actors (id, name, birth_year) VALUES
(1, '송강호', 1967),
(2, '이병헌', 1970),
(3, '전도연', 1973),
(4, '마동석', 1971),
(5, '김혜수', 1970),
(6, '유아인', 1986),
(7, '정우성', 1973),
(8, '김윤석', 1968);

-- MovieGenres (장르 데이터)
INSERT INTO MovieGenres (id, genre_name) VALUES
(1, '드라마'),
(2, '스릴러'),
(3, '액션'),
(4, '코미디'),
(5, 'SF');

-- Movies (영화 데이터)
INSERT INTO Movies (id, title, release_year, director_ref) VALUES
(1, '기생충', 2019, 1),
(2, '괴물', 2006, 1),
(3, '올드보이', 2003, 2),
(4, '아가씨', 2016, 2),
(5, '명량', 2014, 3),
(6, '한산: 용의 출현', 2022, 3),
(7, '부산행', 2016, 4),
(8, '비상선언', 2022, 4),
(9, '버닝', 2018, 5),
(10, '밀양', 2007, 5);

-- MovieActorMapping (영화와 배우 매핑)
INSERT INTO MovieActorMapping (movie_id, actor_id) VALUES
(1, 1), (1, 6),
(2, 1), (2, 5),
(3, 6), (3, 7),
(4, 6), (4, 3),
(5, 2), (5, 8),
(6, 2), (6, 8),
(7, 4), (7, 6),
(8, 1), (8, 4),
(9, 6), (9, 3),
(10, 3), (10, 5);

-- MovieGenreMapping (영화와 장르 매핑)
INSERT INTO MovieGenreMapping (movie_id, genre_id) VALUES
(1, 1),(1, 2),
(2, 2),(2, 3),
(3, 2),(4, 1),
(5, 3),(6, 3),
(7, 5),(7, 2),
(8, 3),(8, 1),
(9, 1),(10, 1);
