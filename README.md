# Spring Boot & LangChain4j RAG 챗봇

Java 백엔드 개발자로서 최신 Gen AI 기술 스택(LLM, RAG)에 대한 학습을 위해 구현한 개인 미니 프로젝트입니다.

Spring Boot, LangChain4j, Google Gemini API를 사용하여, 개인 이력서(PDF)의 내용을 기반으로 질문에 답변하는 RAG(검색 증강 생성) 챗봇을 구현했습니다.

---

## 1. 사용 기술

* **Backend:** Java 17, Spring Boot 3.x
* **AI (Orchestration):** LangChain4j
* **AI (LLM):** Google Gemini (`gemini-1.5-flash`, `embedding-001`)
* **Frontend:** `index.html`
* **Build:** Gradle

---

## 2. 핵심 아키텍처

1.  **PDF Ingestion:** 애플리케이션 실행 시, `ApachePdfBoxDocumentParser`가 `src/main/resources`의 PDF 파일을 읽어 **1개의 `Document` 객체**로 로드합니다.
2.  **Vectorization:** `GoogleAiGeminiEmbeddingModel`이 이 `Document` 전체를 1개의 `TextSegment`로 변환하고 벡터화합니다.
3.  **Storage:** `InMemoryEmbeddingStore`가 이 1개의 벡터를 메모리에 저장합니다.
4.  **RAG Pipeline:**
    * 사용자가 `index.html` UI에서 질문을 전송합니다.
    * `ChatController`가 `/chat` (POST) 요청을 받습니다.
    * `RetrievalAugmentor`가 질문과 가장 유사한 벡터를 DB에서 검색합니다.
    * `AiServices`가 `@SystemMessage` (기본 지시사항) + 검색된 이력서 내용 + 사용자 질문(`@UserMessage`)을 조합하여 `GoogleAiGeminiChatModel`에 전달합니다.
    * AI의 답변이 UI로 반환됩니다.

---

## 3. 실행 화면

`http://localhost:8080`으로 접속하여 `index.html` UI를 통해 이력서 기반 질의응답을 테스트할 수 있습니다.

<img width="621" height="563" alt="image" src="https://github.com/user-attachments/assets/5f1bad8f-200e-4783-9ea4-5df84a37ff24" />


`[실행 화면 예시 스크린샷]`

---

## 4. 실행 방법

1.  Google AI Studio에서 API 키를 발급받습니다.
2.  프로젝트를 실행하는 머신에 `AI_API_KEY`라는 이름으로 **환경 변수**를 설정합니다.
3.  `src/main/resources`에 질의응답의 기반이 될 PDF 파일을 (`.gitignore`에 추가한 파일명과 동일하게) 위치시킵니다. (*개인정보 문제로 이력서 파일은 첨부하지 않았습니다.)
4.  `./gradlew bootRun` 또는 IntelliJ에서 애플리케이션을 실행합니다.

---

## 5. 주요 트러블슈팅 로그

* **문제:** PDF 문서를 청크로 분할하여 `EmbeddingStore`에 저장했을 때, AI의 답변이 부정확하고 관련 없는 내용을 반환하는 현상 발생.
    * *(예: 'A회사' 경력을 질문해도 'B회사' 경력을 답변함)*
* **원인:** 이력서의 'A회사' 경력과 'B회사' 경력 등, 논리적으로 분리된 섹션이 작은 청크 단위로 쪼개지면서 문맥 일관성을 잃음. "B회사"에 대한 질문에도 "컨두잇" 관련 청크가 더 높은 유사도로 검색됨.
* **해결:** RAG 파이프라인의 핵심은 '검색'이므로, 문서의 크기(3-page)가 LLM의 컨텍스트 창보다 충분히 작다고 판단. `DocumentSplitter`를 사용하지 않고 **문서를 통째로 1개의 `TextSegment`로 벡터화**하는 방식으로 변경함.
* **결과:** RAG가 항상 '이력서 전체'를 참고하게 되어, AI가 두 경력을 명확히 구분하고 정확하게 답변하게 됨.

---

## 6. 느낀 점
이번에 만든 챗봇은 이력서 분량이 3장 밖에 되지 않았기 때문에 데이터를 나누지 않고 통째로 DB에 저장하여 AI에 넘기는 게 오히려 정확도가 높았습니다.

하지만 만약 참고할 문서가 수백 페이지처럼 엄청 커진다면 이러한 방법의 한계는 분명히 존재할 것입니다.

1. 파일을 통째로 저장하기엔 메모리(인메모리 DB)도 부족할 것이고,

2. 무엇보다 AI가 매번 수백 페이지를 다 읽느라 너무 느리고 API 비용도 많이 들 것입니다.

그래서 '어디에 문서를 저장하고', '어떻게 똑똑하게 문서를 잘 나누고', '어떻게 질문에 맞는 내용을 콕 집어 잘 찾아올지'를 고민하고 튜닝하는 작업이 꼭 필요할 것이라고 판단했습니다.
