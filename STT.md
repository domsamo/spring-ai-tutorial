# STT (Speech-to-Text), TTS(Text-to-Speech)

LLM을 통해 음성파일 -> 텍스트로 생성

### Chapter 4. STT (Speech-to-Text)

#### 1) 음성파일 생성(.mp3) 

[https://ttsmp3.com/](https://ttsmp3.com/)

#### 2) schema + data 자동 생성
```java
@PostMapping("/transcribe")
public ResponseEntity<String> transcribe(@RequestParam("file") MultipartFile file) throws Exception {
    Resource resource = file.getResource();
    // Set transcription options
    OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
            .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)   //default:json
            .language("ko")
            .temperature(0f)
            //.responseFormat(this.responseFormat)
            .build();

    // Create a transcription prompt
    AudioTranscriptionPrompt audioTranscriptionPrompt =
            new AudioTranscriptionPrompt(resource, options);

    // Call the transcription API
    AudioTranscriptionResponse audioTranscriptionResponse = openAiAudioTranscriptionModel.call(audioTranscriptionPrompt);

    // Return the transcribed text
    return new ResponseEntity<>(audioTranscriptionResponse.getResult().getOutput(), HttpStatus.OK);
}
```

### Chapter 4-1. TTS (Text-to-Speech)

LLM을 통해 텍스트 데이터  -> 음성파일로 생성

#### 1) 텍스터 데이터
```text
# resources
tts.txt
```

#### 2) schema + data 자동 생성
```java
@PostMapping("/upload")                                                                                                     //     tts.txt
public ResponseEntity<StreamingResponseBody> uploadFile(@RequestParam("file") MultipartFile file)
        throws IOException {
    // 업로드된 파일의 텍스트 내용 읽기
    String content = new String(file.getBytes(), StandardCharsets.UTF_8);

    OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
            .voice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
            .speed(1.1f)
            .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
            .model(OpenAiAudioApi.TtsModel.TTS_1.value)
            .build();

    SpeechPrompt speechPrompt = new SpeechPrompt(content, options);

    // 리액티브 스트림 생성(실시간 오디오 스트리밍) - LLM(text)--->Auido(mp3)
    Flux<SpeechResponse> responseStream = openAiAudioSpeechModel.stream(speechPrompt);

    // StreamingResponseBody로 변환하여 클라이언트로 스트림 반환
    StreamingResponseBody stream = outputStream ->
            responseStream.toStream().forEach(speechResponse -> writeToOutput(outputStream, speechResponse));

    return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg") // MP3 파일로 설정
            .body(stream); // byte[]/ byte[], byte[]
}
```
