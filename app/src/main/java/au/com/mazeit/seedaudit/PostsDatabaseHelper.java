package au.com.mazeit.seedaudit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Created by mr on 14/03/16.
 */


public class PostsDatabaseHelper extends SQLiteOpenHelper {
    // Database Info
    private static final String DATABASE_NAME = "SeedAuditDatabase";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_POSTS = "posts";
    private static final String TABLE_USERS = "users";
    private static final String TABLE_SEEDLOTS = "seedlot";
    private static final String TABLE_SEEDAUDIT = "seedlotAudit";
    private static final String TABLE_VFOFFICE = "vfOffice";


    // Post Table Columns
    private static final String KEY_POST_ID = "id";
    private static final String KEY_POST_USER_ID_FK = "userId";
    private static final String KEY_POST_TEXT = "text";

    // User Table Columns
    private static final String KEY_USER_ID = "id";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_PROFILE_PICTURE_URL = "profilePictureUrl";

    // seedlot Table Columns
    private static final String KEY_SEEDLOT_ID = "id";
    private static final String KEY_SEEDLOT_SLN_ID = "slnID";
    private static final String KEY_SEEDLOT_NAME = "seedlotName";
    private static final String KEY_SEEDLOT_NUMBER = "seedlotNumber";
    private static final String KEY_SEEDLOT_OFFICE = "locationID";

    // seedlotAudit Table Columns
    private static final String KEY_SA_ID = "id";
    private static final String KEY_SA_SLN_ID = "slnID";
    private static final String KEY_SA_OFFICE = "locationID";
    private static final String KEY_SA_GROSS = "grossWgt";
    private static final String KEY_SA_BARREL = "barrelWgt";
    private static final String KEY_SA_NETT = "nett";
    private static final String KEY_SA_VERIFIED = "verifiedIND";
    private static final String KEY_SA_CHANGEDBY = "changedBy";
    private static final String KEY_SA_CHANGEDDATE = "changedDate";

    // vfOffice Table Columns
    private static final String KEY_VFOFFICE_ID = "id";
    private static final String KEY_VFOFFICE_LOCATION_ID = "locationID";
    private static final String KEY_VFOFFICE_NAME = "vfOfficeName";

    // Used by XML parser
    private static final String ns = null;

    private static PostsDatabaseHelper sInstance;

    public static synchronized PostsDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new PostsDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private PostsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_POSTS_TABLE = "CREATE TABLE " + TABLE_POSTS +
                "(" +
                KEY_POST_ID + " INTEGER PRIMARY KEY, " + // Define a primary key
                KEY_POST_USER_ID_FK + " INTEGER REFERENCES " + TABLE_USERS + "," + // Define a foreign key
                KEY_POST_TEXT + " TEXT" +
                ")";

        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS +
                "(" +
                KEY_USER_ID + " INTEGER PRIMARY KEY, " +
                KEY_USER_NAME + " TEXT, " +
                KEY_USER_PROFILE_PICTURE_URL + " TEXT" +
                ")";

        String CREATE_SEEDLOT_TABLE = "CREATE TABLE " + TABLE_SEEDLOTS +
                "(" +
                KEY_SEEDLOT_ID + " INTEGER PRIMARY KEY, " +
                KEY_SEEDLOT_SLN_ID + " INTEGER, " +
                KEY_SEEDLOT_NAME + " TEXT, " +
                KEY_SEEDLOT_NUMBER + " TEXT, " +
                KEY_SEEDLOT_OFFICE + " INTEGER" +
                ")";

        String CREATE_SEEDAUDIT_TABLE = "CREATE TABLE " + TABLE_SEEDAUDIT +
                "(" +
                KEY_SA_ID + " INTEGER PRIMARY KEY, " +
                KEY_SA_SLN_ID + " INTEGER, " +
                KEY_SA_OFFICE + " INTEGER, " +
                KEY_SA_GROSS + " REAL, " +
                KEY_SA_BARREL + " REAL, " +
                KEY_SA_NETT + " REAL, " +
                KEY_SA_VERIFIED + " INTEGER, " +
                KEY_SA_CHANGEDBY + " TEXT, " +
                KEY_SA_CHANGEDDATE + " TEXT" +
                ")";


        String CREATE_VFOFFICE_TABLE = "CREATE TABLE " + TABLE_VFOFFICE +
                "(" +
                KEY_VFOFFICE_ID + " INTEGER PRIMARY KEY, " +
                KEY_VFOFFICE_LOCATION_ID + " INTEGER, " +
                KEY_VFOFFICE_NAME + " TEXT" +
                ")";

        db.execSQL(CREATE_POSTS_TABLE);
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_SEEDLOT_TABLE);
        db.execSQL(CREATE_SEEDAUDIT_TABLE);
        db.execSQL(CREATE_VFOFFICE_TABLE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_VFOFFICE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEEDAUDIT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEEDLOTS);
            onCreate(db);
        }
    }


    public class Post {
        public User user;
        public String text;
    }


    public class User {
        public String userName;
        public String profilePictureUrl;
    }


//    public class SeedAudit {
//        public int slnId;
//        public int locationId;
//        public float gross;
//        public float barrelWgt;
//        public float nett;
//        public int verified;
//        public String changedBy;
//        public String ChangedDate;
//    }

    public void addSeedAudit(SeedAudit seedAudit){
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            // The user might already exist in the database (i.e. the same user created multiple posts).
            //long userId = addOrUpdateUser(seedaudit.);

            ContentValues values = new ContentValues();
            //values.put(KEY_POST_USER_ID_FK, userId);
            values.put(KEY_SA_SLN_ID, seedAudit.slnId);
            values.put(KEY_SA_OFFICE, seedAudit.locationId);
            values.put(KEY_SA_GROSS, seedAudit.gross);
            values.put(KEY_SA_BARREL, seedAudit.barrelWgt);
            values.put(KEY_SA_NETT, seedAudit.nett);
            values.put(KEY_SA_VERIFIED, seedAudit.verified);
            values.put(KEY_SA_CHANGEDBY, seedAudit.changedBy);
            values.put(KEY_SA_CHANGEDDATE, seedAudit.ChangedDate);



            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertOrThrow(TABLE_SEEDAUDIT, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("PostDatabaseHelper", "Error while trying to add seed audit to database");
        } finally {
            db.endTransaction();
        }
    }

    public void addSeedLot(Seedlot seedLot){
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            // The user might already exist in the database (i.e. the same user created multiple posts).
            //long userId = addOrUpdateUser(seedaudit.);

            ContentValues values = new ContentValues();
            //values.put(KEY_SEEDLOT_ID, id);
            values.put(KEY_SEEDLOT_SLN_ID, seedLot.slnId);
            values.put(KEY_SEEDLOT_NAME, seedLot.name);
            values.put(KEY_SEEDLOT_NUMBER, seedLot.number);
            values.put(KEY_SEEDLOT_OFFICE, seedLot.office);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertOrThrow(TABLE_SEEDLOTS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("PostDatabaseHelper", "Error while trying to add seed lot to database");
        } finally {
            db.endTransaction();
        }
    }

    // Insert a post into the database
    public void addPost(Post post) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            // The user might already exist in the database (i.e. the same user created multiple posts).
            long userId = addOrUpdateUser(post.user);

            ContentValues values = new ContentValues();
            values.put(KEY_POST_USER_ID_FK, userId);
            values.put(KEY_POST_TEXT, post.text);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertOrThrow(TABLE_POSTS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("PostDatabaseHelper", "Error while trying to add post to database");
        } finally {
            db.endTransaction();
        }
    }

    // Insert or update a user in the database
    // Since SQLite doesn't support "upsert" we need to fall back on an attempt to UPDATE (in case the
    // user already exists) optionally followed by an INSERT (in case the user does not already exist).
    // Unfortunately, there is a bug with the insertOnConflict method
    // (https://code.google.com/p/android/issues/detail?id=13045) so we need to fall back to the more
    // verbose option of querying for the user's primary key if we did an update.
    public long addOrUpdateUser(User user) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long userId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_USER_NAME, user.userName);
            values.put(KEY_USER_PROFILE_PICTURE_URL, user.profilePictureUrl);

            // First try to update the user in case the user already exists in the database
            // This assumes userNames are unique
            int rows = db.update(TABLE_USERS, values, KEY_USER_NAME + "= ?", new String[]{user.userName});

            // Check if update succeeded
            if (rows == 1) {
                // Get the primary key of the user we just updated
                String usersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                        KEY_USER_ID, TABLE_USERS, KEY_USER_NAME);
                Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{String.valueOf(user.userName)});
                try {
                    if (cursor.moveToFirst()) {
                        userId = cursor.getInt(0);
                        db.setTransactionSuccessful();
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            } else {
                // user with this userName did not already exist, so insert new user
                userId = db.insertOrThrow(TABLE_USERS, null, values);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.d("PostDatabaseHelper", "Error while trying to add or update user");
        } finally {
            db.endTransaction();
        }
        return userId;
    }

    // Get all posts in the database
    public List<Post> getAllPosts() {
        List<Post> posts = new ArrayList<>();

        // SELECT * FROM POSTS
        // LEFT OUTER JOIN USERS
        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
        String POSTS_SELECT_QUERY =
                String.format("SELECT * FROM %s LEFT OUTER JOIN %s ON %s.%s = %s.%s",
                        TABLE_POSTS,
                        TABLE_USERS,
                        TABLE_POSTS, KEY_POST_USER_ID_FK,
                        TABLE_USERS, KEY_USER_ID);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    User newUser = new User();
                    newUser.userName = cursor.getString(cursor.getColumnIndex(KEY_USER_NAME));
                    newUser.profilePictureUrl = cursor.getString(cursor.getColumnIndex(KEY_USER_PROFILE_PICTURE_URL));

                    Post newPost = new Post();
                    newPost.text = cursor.getString(cursor.getColumnIndex(KEY_POST_TEXT));
                    newPost.user = newUser;
                    posts.add(newPost);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("PostDatabaseHelper", "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return posts;
    }

    // Update the user's profile picture url
    public int updateUserProfilePicture(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_PROFILE_PICTURE_URL, user.profilePictureUrl);

        // Updating profile picture url for user with that userName
        return db.update(TABLE_USERS, values, KEY_USER_NAME + " = ?",
                new String[] { String.valueOf(user.userName) });
    }

    // Delete all posts and users in the database
    public void deleteAllPostsAndUsers() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
            db.delete(TABLE_POSTS, null, null);
            db.delete(TABLE_USERS, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("PostDatabaseHelper", "Error while trying to delete all posts and users");
        } finally {
            db.endTransaction();
        }
    }

    // Delete all data in the supplied table
    public void deleteAllDataInTable(String tableName) {
        // Note Order of deletions is important when foreign key relationships exist.
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(tableName, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("PostDatabaseHelper", "Error while trying to delete all data in table" + tableName);
        } finally {
            db.endTransaction();
        }
    }


    // Get all posts in the database
    public Seedlot getSeedlot(String identifier) {
        Seedlot sRetrieved = new Seedlot();
        sRetrieved.clear();

        String SEEDLOT_SELECT_QUERY =
                String.format("SELECT * FROM %s WHERE %s = %s",
                        TABLE_SEEDLOTS,
                        KEY_SEEDLOT_SLN_ID,
                        identifier);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SEEDLOT_SELECT_QUERY, null);

        try {
            if (cursor.moveToFirst()) {
                do {

                    sRetrieved.slnId = cursor.getInt(cursor.getColumnIndex(KEY_SEEDLOT_SLN_ID));
                    sRetrieved.name = cursor.getString(cursor.getColumnIndex(KEY_SEEDLOT_NAME));
                    sRetrieved.number = cursor.getString(cursor.getColumnIndex(KEY_SEEDLOT_NUMBER));
                    sRetrieved.office = cursor.getInt(cursor.getColumnIndex(KEY_SEEDLOT_OFFICE));
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("PostDatabaseHelper", "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return sRetrieved;
    }


    //public int slnId;
    //public int locationId;
    //public float gross;
    //public float barrelWgt;
    //public float nett;
    //public boolean verified;
    //public String changedBy;
    //public String ChangedDate;

    //private static final String KEY_SA_ID = "id";
    //private static final String KEY_SA_SLN_ID = "slnID";
    //private static final String KEY_SA_OFFICE = "locationID";
    //private static final String KEY_SA_GROSS = "grossWgt";
    //private static final String KEY_SA_BARREL = "barrelWgt";
    //private static final String KEY_SA_NETT = "nett";
    //private static final String KEY_SA_VERIFIED = "verifiedIND";
    //private static final String KEY_SA_CHANGEDBY = "changedBy";
    //private static final String KEY_SA_CHANGEDDATE = "changedDate";

    public int exportSeedAuditsCSV(String dstPath,String dstFile) {
        String[] columns = {KEY_SA_SLN_ID, KEY_SA_OFFICE, KEY_SA_GROSS, KEY_SA_BARREL, KEY_SA_NETT, KEY_SA_VERIFIED, KEY_SA_CHANGEDBY, KEY_SA_CHANGEDDATE};
        String SEEDAUDIT_SELECT_QUERY = String.format("SELECT * FROM %s",TABLE_SEEDAUDIT);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SEEDAUDIT_SELECT_QUERY, null);
        File csvFile = new File(dstPath,dstFile);
        FileWriter fOut = null;
        CSVWriter csvw = null;
        String[] row = new String[8];
        try {
            fOut = new FileWriter(csvFile);
            csvw = new CSVWriter(fOut);
            for (int i = 0; i < columns.length; i++) {
                row[i] = columns[i];
            }
            csvw.writeNext(row);
            if (cursor.moveToFirst()) {
                do {
                    row[0] = Integer.toString(cursor.getInt(cursor.getColumnIndex(KEY_SA_SLN_ID)));
                    row[1] = Integer.toString(cursor.getInt(cursor.getColumnIndex(KEY_SA_OFFICE)));
                    //row[2] = Float.toString(cursor.getFloat(cursor.getColumnIndex(KEY_SA_GROSS)));
                    //row[3] = Float.toString(cursor.getFloat(cursor.getColumnIndex(KEY_SA_BARREL)));
                    //row[4] = Float.toString(cursor.getFloat(cursor.getColumnIndex(KEY_SA_NETT)));
                    row[2] = Double.toString(cursor.getDouble(cursor.getColumnIndex(KEY_SA_GROSS)));
                    row[3] = Double.toString(cursor.getDouble(cursor.getColumnIndex(KEY_SA_BARREL)));
                    row[4] = Double.toString(cursor.getDouble(cursor.getColumnIndex(KEY_SA_NETT)));
                    if(cursor.getInt(cursor.getColumnIndex(KEY_SA_VERIFIED))==1) {
                        row[5] = "true";
                    } else {
                        row[5] = "false";
                    }
                    row[6] = cursor.getString(cursor.getColumnIndex(KEY_SA_CHANGEDBY));
                    row[7] = cursor.getString(cursor.getColumnIndex(KEY_SA_CHANGEDDATE));
                    csvw.writeNext(row);
                } while(cursor.moveToNext());
            }
            csvw.close();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("PostDatabaseHelper", "Error while trying to get audits from database");
        }
        return 1;
    }

    public int exportSeedAudits(String dstPath,String dstFile) {

        String SEEDAUDIT_SELECT_QUERY = String.format("SELECT * FROM %s",TABLE_SEEDAUDIT);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SEEDAUDIT_SELECT_QUERY, null);
        File file = new File(dstPath,dstFile);
        BufferedOutputStream outbuf = null;

        try {


            outbuf = new BufferedOutputStream(new FileOutputStream(file));

            XmlSerializer xs = Xml.newSerializer();
            StringWriter w = new StringWriter();
            xs.setOutput(w);

            xs.startDocument("UTF-8", true);
            xs.startTag(null, "seed_audit_list");

            if (cursor.moveToFirst()) {

                do {
                    xs.startTag(null, "seed_audit");
                    addXml(xs, "slnId", Integer.toString(cursor.getInt(cursor.getColumnIndex(KEY_SA_SLN_ID))));
                    addXml(xs, "locationId", Integer.toString(cursor.getInt(cursor.getColumnIndex(KEY_SA_OFFICE))));
                    //addXml(xs, "gross", Float.toString(cursor.getFloat(cursor.getColumnIndex(KEY_SA_GROSS))));
                    //addXml(xs, "barrelWgt",Float.toString(cursor.getFloat(cursor.getColumnIndex(KEY_SA_BARREL))));
                    //addXml(xs, "nett",Float.toString(cursor.getFloat(cursor.getColumnIndex(KEY_SA_NETT))));
                    addXml(xs, "gross", Double.toString(cursor.getDouble(cursor.getColumnIndex(KEY_SA_GROSS))));
                    addXml(xs, "barrelWgt",Double.toString(cursor.getDouble(cursor.getColumnIndex(KEY_SA_BARREL))));
                    addXml(xs, "nett",Double.toString(cursor.getDouble(cursor.getColumnIndex(KEY_SA_NETT))));
                    if(cursor.getInt(cursor.getColumnIndex(KEY_SA_VERIFIED))==1) {
                        addXml(xs, "verified","true");
                    } else {
                        addXml(xs, "verified","false");
                    }
                    addXml(xs, "changedBy",cursor.getString(cursor.getColumnIndex(KEY_SA_CHANGEDBY)));
                    addXml(xs, "ChangedDate",cursor.getString(cursor.getColumnIndex(KEY_SA_CHANGEDDATE)));
                    xs.endTag(null, "seed_audit");
                } while(cursor.moveToNext());
            }
            xs.endTag(null, "seed_audit_list");
            xs.endDocument();
            xs.flush();
            outbuf.write(w.toString().getBytes());
            outbuf.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.d("PostDatabaseHelper", "Error while trying to get audits from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return 1;
    }

    private void addXml(XmlSerializer xml,String tag,String data) throws IOException {
        xml.startTag(null, tag);
        xml.text(data);
        xml.endTag(null, tag);
    }

    // Get all posts in the database
    public void dumpSeedlot() {
        Seedlot sRetrieved = new Seedlot();

        String SEEDLOT_SELECT_QUERY = String.format("SELECT * FROM %s",TABLE_SEEDLOTS);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SEEDLOT_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    sRetrieved.slnId = cursor.getInt(cursor.getColumnIndex(KEY_SEEDLOT_SLN_ID));
                    sRetrieved.name = cursor.getString(cursor.getColumnIndex(KEY_SEEDLOT_NAME));
                    sRetrieved.number = cursor.getString(cursor.getColumnIndex(KEY_SEEDLOT_NUMBER));
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("PostDatabaseHelper", "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }


    public void importSeedlotsCSV(String importPath, String importFile) {
        try {
            File eventlistFile = new File(importPath,importFile);
            FileInputStream fIn = new FileInputStream(eventlistFile);
            parseSeedLotsCSV(fIn);
            fIn.close();
        }
        catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void parseSeedLotsCSV(InputStream in) throws IOException {
        try {
            InputStreamReader isreader = new InputStreamReader(in);
            CSVReader reader = new CSVReader(isreader);
            String [] nextLine;
            deleteAllDataInTable(TABLE_SEEDLOTS);
            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                if(nextLine[0].equals("SlnID")) {
                    continue;
                }
                else {
                    Seedlot sl = new Seedlot();
                    sl.slnId  = Integer.parseInt(nextLine[0]);
                    sl.number = nextLine[1];
                    sl.name   = nextLine[2];
                    sl.office = Integer.parseInt(nextLine[5]);
                    addSeedLot(sl);
                }
            }
        } finally {
            in.close();
        }
    }

    public void importSeedlots(String importPath, String importFile) {
        try {
            File eventlistFile = new File(importPath,importFile);
            FileInputStream fIn = new FileInputStream(eventlistFile);
            parseSeedLots(fIn);
            fIn.close();
        }
        catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void parseSeedLots(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            deleteAllDataInTable(TABLE_SEEDLOTS);
            readSeedLots(parser);
        } finally {
            in.close();
        }
    }

    private void readSeedLots(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "seedlot_list");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("seedlot")) {
                readSeedLot(parser);
            } else {
                skip(parser);
            }
        }
    }

    private void readSeedLot(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "seedlot");
        Seedlot sl = new Seedlot();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("slnID")) {
                sl.slnId = Integer.parseInt(readTag("slnID",parser));
            } else if (name.equals("seedlotName")) {
                sl.name = readTag("seedlotName",parser);
            } else if (name.equals("seedlotNumber")) {
                sl.number = readTag("seedlotNumber",parser);
            } else if (name.equals("locationID")) {
                sl.office = Integer.parseInt(readTag("locationID", parser));
            } else {
                skip(parser);
            }
        }
        addSeedLot(sl);
    }

    private String readTag(String tag,XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, tag);
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, tag);
        return title;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}
