package seb45_main_029.server.exception;

import lombok.Getter;

public enum ExceptionCode {
    USER_NOT_FOUND(404, "User not found"),
    //User not in database.
    USER_EXISTS(409, "User exists"),
    //    EMAIL_EXISTS(409, "Email exists"),
    PASSWORD_NOT_MATCH(404, "Password does not match"),
    //    NICKNAME_EXISTS(409, "Nickname exists"),
    QUESTION_NOT_FOUND(404, "Question not found"),
    //    QUESTION_AUTHOR_NOT_MATCH(404, "The author of the question does not match"),
    ANSWER_NOT_FOUND(404, "Answer not found"),
    //    ANSWER_AUTHOR_NOT_MATCH(404,"The author of the answer does not match");
    UNAUTHORIZED_USER(403, "Unauthorized user"),
    //unauthorized_user


    VIDEO_NOT_FOUND(404, "Video not found"),

    BOOKMARK_VIDEO_NOT_FOUND(404, "Bookmark Video Not found"),

    POST_NOT_FOUND(404, "Post Not found"),

    PRODUCT_NOT_FOUND(404, "Product Not found"),

    NOT_ENOUGH_POINTS(404, "Not Enough Points"),

    IMAGE_NOT_FOUND(404, "Image Not found"),

    IMAGE_EXISTS(409,"Image exists");

    @Getter
    private int status;

    @Getter
    private String message;

    ExceptionCode(int code, String message) {
        this.status = code;
        this.message = message;
    }
}
