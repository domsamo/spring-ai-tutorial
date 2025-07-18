# Image Model

주어진 프롬프트로 이미지 생성 및 수정 기능을 제공

### Chapter 3. Image Model

#### 1) OpenAI (DALL-2, DALL-3) 활용 

- 챗봇으로 이미지 생성 (http://localhost:8080/image)
```text
DALL-3 : 이미지 갯수 제한(Max 1개)
DALL-2 : 이미지 갯수 제한(Max 3개), quality 선택 안됨.
```
```java
@Override
public ImageResponse getImageGen(ImageRequestDTO request) {
    log.info("request.getModel() : {}", request.getModel());
    OpenAiImageOptions options = null;

    if("dall-e-3".equals(request.getModel())){
        options =  OpenAiImageOptions.builder()
                .model(request.getModel())
                .quality("hd")              // DALL-E 3에서 사용 가능
                .N(1)                   // DALL-E 3는 n=1만 지원
                .height(1024)
                .width(1024)
                .build();
    }else{
        options =  OpenAiImageOptions.builder()
                .model(request.getModel())
                .N(request.getN())          // DALL-E 3는 n=1만 지원
                .height(1024)
                .width(1024)
                .build();
    }

    ImageResponse imageResponse = openAiImageModel
            .call(new ImagePrompt(request.getMessage(), options));
    return  imageResponse;
}
```

- 음성 챗봇으로 이미지 생성 (http://localhost:8080/imagevoice)

```javascript
// 음성 인식 시작
function startSpeechRecognition() {
    recognition = new (window.SpeechRecognition || window.webkitSpeechRecognition)();
    recognition.lang = 'ko-KR'; // 한국어 설정
    recognition.continuous = false; // 한 번만 인식
    recognition.interimResults = false; // 중간 결과 비활성화

    recognition.onstart = () => {
        console.log("음성 인식 시작");
        document.getElementById("status").innerText = "음성 인식 중...";
    };

    recognition.onresult = (event) => {
        const transcript = event.results[0][0].transcript; // 첫 번째 결과
        console.log("음성 인식 결과:", transcript);

        // 텍스트 박스에 음성 인식 결과 입력
        document.getElementById("message").value = transcript;
        document.getElementById("status").innerText = "음성 인식 완료";

        // 텍스트 길이가 10자 이상일 경우 generateImage() 호출
        if (transcript.length >= 10) {
            generateImage();
        } else {
            document.getElementById("status").innerText += " - 텍스트 길이가 너무 짧습니다.";
        }
    };

    recognition.onerror = (event) => {
        console.error("오류 발생:", event.error);
        document.getElementById("status").innerText = "오류 발생: " + event.error;
    };

    recognition.onend = () => {
        console.log("음성 인식 종료");
        document.getElementById("status").innerText = "음성 인식이 종료되었습니다.";
    };

    recognition.start(); // 음성 인식 시작
}
```

### Chapter 4. Image 분석 

- AI를 통해 이미지를 분석 (http://localhost:8080/imageview)
```java
@Override
public String analyzeImage(MultipartFile imageFile, String message) throws IOException {

    var media = new Media(MimeType.valueOf(contentType), imageFile.getResource());
    var userMessage = new UserMessage(message, media);
    var systemMessage = new SystemMessage(imageSystemMessage);
    return chatModel.call(userMessage, systemMessage);
}
```

- 수학 문제 풀이 (http://localhost:8080/imagemath)

수학문제 이미지에 대한 풀이와 **핵심 키워드**에 대한 Youtube 검색을 통해 동영상 링크


LaTeX(레이텍) 수학 공식을 MathJax(매스잭스)로 변환
```javascript
const data = await response.json();

// Display the analysis results
document.getElementById("resultSection").style.display = "block";
document.getElementById("uploadedImage").src = data.imageUrl;
document.getElementById("analysisText").innerText = data.analysisText;
MathJax.typeset();
```

