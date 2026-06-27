CREATE TABLE `users`
(
    `id`                 BIGINT       NOT NULL,
    `email`              VARCHAR(255) NOT NULL,
    `password`           VARCHAR(255) NOT NULL,
    `social_provider`    ENUM('NONE', 'GOOGLE', 'KAKAO') NOT NULL,
    `social_id`          VARCHAR(255) NOT NULL,
    `name`               VARCHAR(100) NOT NULL,
    `role`               VARCHAR(100) NOT NULL,
    `profile_image_url`  VARCHAR(500) NOT NULL,
    `tech_tags`          VARCHAR(500) NOT NULL,
    `participation_type` ENUM('CONTEST', 'PERSONAL_PROJECT', 'BOTH') NOT NULL,
    `introduction`       VARCHAR(255) NULL,
    `portfolio_url`      VARCHAR(500) NULL,
    `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at`         DATETIME NULL,

    CONSTRAINT `PK_USERS` PRIMARY KEY (`id`)
);

CREATE TABLE `roles`
(
    `id`   BIGINT       NOT NULL,
    `name` VARCHAR(100) NOT NULL,

    CONSTRAINT `PK_ROLES` PRIMARY KEY (`id`)
);

CREATE TABLE `projects`
(
    `id`            BIGINT       NOT NULL,
    `activity_type` ENUM('CONTEST', 'PERSONAL_PROJECT') NOT NULL,
    `title`         VARCHAR(255) NOT NULL,
    `description`   TEXT         NOT NULL,
    `project_name`  VARCHAR(255) NULL,
    `project_url`   VARCHAR(500) NULL,
    `start_date`    DATE         NOT NULL,
    `end_date`      DATE         NOT NULL,
    `deadline`      DATE         NOT NULL,
    `meeting_type`  ENUM('ONLINE', 'OFFLINE', 'HYBRID') NOT NULL,
    `tags`          VARCHAR(255) NOT NULL,
    `status`        ENUM('RECRUITING', 'CLOSED', 'IN_PROGRESS', 'COMPLETED') NOT NULL,
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at`    DATETIME NULL,
    `user_id`       BIGINT       NOT NULL,

    CONSTRAINT `PK_PROJECTS` PRIMARY KEY (`id`),
    CONSTRAINT `FK_PROJECTS_USER`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);

CREATE TABLE `project_roles`
(
    `id`            BIGINT NOT NULL,
    `recruit_count` INT    NOT NULL,
    `project_id`    BIGINT NOT NULL,
    `role_id`       BIGINT NOT NULL,

    CONSTRAINT `PK_PROJECT_ROLES` PRIMARY KEY (`id`),
    CONSTRAINT `UK_PROJECT_ROLE`
        UNIQUE (`project_id`, `role_id`),
    CONSTRAINT `FK_PROJECT_ROLES_PROJECT`
        FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`),
    CONSTRAINT `FK_PROJECT_ROLES_ROLE`
        FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
);

CREATE TABLE `applications`
(
    `id`         BIGINT   NOT NULL,
    `message`    TEXT     NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status`     ENUM('PROGRESS', 'PASS', 'FAIL', 'CANCEL') NOT NULL DEFAULT 'PROGRESS',
    `user_id`    BIGINT   NOT NULL,
    `project_id` BIGINT   NOT NULL,
    `role_id`    BIGINT   NOT NULL,

    CONSTRAINT `PK_APPLICATIONS` PRIMARY KEY (`id`),
    CONSTRAINT `UK_APPLICATION_USER_PROJECT`
        UNIQUE (`user_id`, `project_id`),
    CONSTRAINT `FK_APPLICATION_USER`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `FK_APPLICATION_PROJECT`
        FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`),
    CONSTRAINT `FK_APPLICATION_ROLE`
        FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
);

CREATE TABLE `bookmark`
(
    `id`         BIGINT NOT NULL,
    `user_id`    BIGINT NOT NULL,
    `project_id` BIGINT NOT NULL,

    CONSTRAINT `PK_BOOKMARK` PRIMARY KEY (`id`),
    CONSTRAINT `UK_BOOKMARK_USER_PROJECT`
        UNIQUE (`user_id`, `project_id`),
    CONSTRAINT `FK_BOOKMARK_USER`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `FK_BOOKMARK_PROJECT`
        FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`)
);

CREATE TABLE `notifications`
(
    `id`             BIGINT       NOT NULL,
    `type`           ENUM('NEW_APPLICANT', 'APPLICATION_RESULT', 'DEADLINE_APPROACHING', 'SYSTEM') NOT NULL,
    `content`        VARCHAR(500) NOT NULL,
    `is_read`        BOOLEAN      NOT NULL DEFAULT FALSE,
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `user_id`        BIGINT       NOT NULL,
    `application_id` BIGINT NULL,

    CONSTRAINT `PK_NOTIFICATIONS` PRIMARY KEY (`id`),
    CONSTRAINT `FK_NOTIFICATION_USER`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `FK_NOTIFICATION_APPLICATION`
        FOREIGN KEY (`application_id`) REFERENCES `applications` (`id`)
);