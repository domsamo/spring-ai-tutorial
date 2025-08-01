-- Authors 테이블 생성 (작가 정보를 저장하는 테이블)
CREATE TABLE Authors (
    id INT NOT NULL AUTO_INCREMENT COMMENT '작가의 고유 ID',
    name VARCHAR(255) NOT NULL COMMENT '작가의 이름',
    PRIMARY KEY (id)
);

-- Publishers 테이블 생성 (출판사 정보를 저장하는 테이블)
CREATE TABLE Publishers (
    id INT NOT NULL AUTO_INCREMENT COMMENT '출판사의 고유 ID',
    name VARCHAR(255) NOT NULL COMMENT '출판사의 이름',
    PRIMARY KEY (id)
);

-- Books 테이블 생성 (책 정보를 저장하는 테이블)
CREATE TABLE Books (
   id INT NOT NULL AUTO_INCREMENT COMMENT '책의 고유 ID',
   title VARCHAR(255) NOT NULL COMMENT '책의 제목',
   author_ref INT NOT NULL COMMENT '책의 저자 ID (Authors 테이블 참조)',
   publisher_ref INT NOT NULL COMMENT '책의 출판사 ID (Publishers 테이블 참조)',
   PRIMARY KEY (id)
);

-- 감독의 기본 정보
CREATE TABLE Directors (
    id INT NOT NULL AUTO_INCREMENT COMMENT '감독의 고유 ID',
    name VARCHAR(255) NOT NULL COMMENT '감독 이름',
    birth_year INT COMMENT '감독의 출생 연도',
    PRIMARY KEY (id)
);
-- 영화의 기본 정보
CREATE TABLE Movies (
    id INT NOT NULL AUTO_INCREMENT COMMENT '영화의 고유 ID',
    title VARCHAR(255) NOT NULL COMMENT '영화 제목',
    release_year INT NOT NULL COMMENT '영화 개봉 연도',
    director_ref INT NOT NULL COMMENT '영화 감독 ID (Directors 테이블 참조)',
    PRIMARY KEY (id),
    FOREIGN KEY (director_ref) REFERENCES Directors(id)
);
-- 배우의 기본 정보
CREATE TABLE Actors (
    id INT NOT NULL AUTO_INCREMENT COMMENT '배우의 고유 ID',
    name VARCHAR(255) NOT NULL COMMENT '배우 이름',
    birth_year INT COMMENT '배우의 출생 연도',
    PRIMARY KEY (id)
);
-- 영화 장르
CREATE TABLE MovieGenres (
    id INT NOT NULL AUTO_INCREMENT COMMENT '영화 장르의 고유 ID',
    genre_name VARCHAR(100) NOT NULL COMMENT '영화 장르 이름',
    PRIMARY KEY (id)
);
-- 영화에 출연한 배우를 연결
CREATE TABLE MovieActorMapping (
    movie_id INT NOT NULL COMMENT '영화 ID (Movies 테이블 참조)',
    actor_id INT NOT NULL COMMENT '배우 ID (Actors 테이블 참조)',
    PRIMARY KEY (movie_id, actor_id),
    FOREIGN KEY (movie_id) REFERENCES Movies(id),
    FOREIGN KEY (actor_id) REFERENCES Actors(id)
);
-- 영화의 장르를 연결
CREATE TABLE MovieGenreMapping (
    movie_id INT NOT NULL COMMENT '영화 ID (Movies 테이블 참조)',
    genre_id INT NOT NULL COMMENT '장르 ID (MovieGenres 테이블 참조)',
    PRIMARY KEY (movie_id, genre_id),
    FOREIGN KEY (movie_id) REFERENCES Movies(id),
    FOREIGN KEY (genre_id) REFERENCES MovieGenres(id)
);
