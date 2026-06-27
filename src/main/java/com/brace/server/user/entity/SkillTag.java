package com.brace.server.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SkillTag {

    // 언어
    JAVA("Java"),
    PYTHON("Python"),
    JAVASCRIPT("JavaScript"),
    TYPESCRIPT("TypeScript"),
    KOTLIN("Kotlin"),
    CPP("C++"),
    CSHARP("C#"),
    GO("Go"),

    // 프론트엔드
    REACT("React"),
    NEXT_JS("Next.js"),
    VUE_JS("Vue.js"),
    ANGULAR("Angular"),
    HTML_CSS("HTML/CSS"),
    TAILWIND_CSS("Tailwind CSS"),

    // 백엔드
    SPRING("Spring"),
    SPRING_BOOT("Spring Boot"),
    DJANGO("Django"),
    FAST_API("FastAPI"),
    NODE_JS("Node.js"),
    EXPRESS("Express"),
    NEST_JS("NestJS"),
    FLASK("Flask"),

    // 모바일
    ANDROID("Android"),
    IOS("iOS"),
    FLUTTER("Flutter"),
    REACT_NATIVE("React Native"),

    // 데이터베이스
    MYSQL("MySQL"),
    POSTGRESQL("PostgreSQL"),
    MONGODB("MongoDB"),
    REDIS("Redis"),
    NEO4J("Neo4j"),

    // 인프라
    AWS("AWS"),
    DOCKER("Docker"),
    KUBERNETES("Kubernetes"),
    NGINX("Nginx"),
    LINUX("Linux"),
    GITHUB_ACTIONS("GitHub Actions"),

    // AI/데이터
    TENSORFLOW("TensorFlow"),
    PYTORCH("PyTorch"),
    PANDAS("Pandas"),
    SCIKIT_LEARN("Scikit-learn"),
    LANGCHAIN("LangChain"),
    OPENAI_API("OpenAI API"),
    UPSTAGE_API("Upstage API"),

    // 디자인
    FIGMA("Figma"),
    PHOTOSHOP("Photoshop"),
    ILLUSTRATOR("Illustrator"),
    BLENDER("Blender"),
    AFTER_EFFECTS("After Effects"),

    // 기획/협업
    NOTION("Notion"),
    JIRA("Jira"),
    CONFLUENCE("Confluence"),
    MIRO("Miro"),
    SLACK("Slack"),
    DISCORD("Discord"),

    // 마케팅
    GOOGLE_ANALYTICS("Google Analytics"),
    GOOGLE_ADS("Google Ads"),
    META_ADS("Meta Ads"),
    SEO("SEO"),
    MIXPANEL("Mixpanel"),
    MAILCHIMP("Mailchimp");

    private final String displayName;
}