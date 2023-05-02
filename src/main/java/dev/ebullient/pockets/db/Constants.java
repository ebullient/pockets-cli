package dev.ebullient.pockets.db;

public class Constants {
    public static final String NATURAL_ID = "slug = ?1";
    public static final String PROFILE_NATURAL_ID_LIKE = "profile.id = ?1 and slug like CONCAT(?2,'%')";

    public static final String PROFILE_ID = "profile.id = ?1";
    public static final String PROFILE_NATURAL_ID = "profile.id = ?1 and slug = ?2";
}
