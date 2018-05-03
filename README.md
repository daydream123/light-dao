## light-dao
### 1. 首先，得定义一个DBHelper，大家都知道的，主要用来创建database和table以及提供database对象的:

```java
public class DBHelper extends BaseDBHelper {
    @SuppressLint("StaticFieldLeak")
    private static DBHelper mSingleton;

    private DBHelper(Context context) {
        super(context, "school.db", 1);
    }

    private static DBHelper getSingleton(Context context) {
        if (mSingleton == null) {
            synchronized (DBHelper.class) {
                mSingleton = new DBHelper(context.getApplicationContext());
            }
        }
        return mSingleton;
    }

    static DBUtils with(Context context){
        return DBUtils.create(getSingleton(context));
    }

    @Override
    protected void onClassLoad(List<Class<? extends BaseTable>> tableClasses) {
        tableClasses.add(Student.class);
    }
}
```

#### 2. 然后，定义你的各种表，如下：

```java
@Table(Student.TABLE_NAME)
public class Student extends BaseTable {
    public static final String TABLE_NAME = "student";

    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_ARTICLE = "article";

    @Column(name = COLUMN_NAME, notnull = true)
    public String name;

    @Column(name = COLUMN_AGE, notnull = true)
    public Integer age;

    @Column(name = COLUMN_ARTICLE)
    public byte[] article;
}
```

#### 3. 最后，就可以尽情使用API进行数据库操作了：

```java
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private Context mContext;

    @Before
    public void before(){
        mContext = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testSave(){
        Student student = new Student();
        student.name = "zhangsan";
        student.age = 20;
        long id = DBHelper.with(mContext).save(student);
        assertTrue(id > 0);
    }

    @Test
    public void testSaveAll(){
        List<Student> students = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Student student = new Student();
            student.name = "name " + i;
            student.age = i;
            students.add(student);
        }
        int count = DBHelper.with(mContext).saveAll(students);
        assertTrue(count == students.size());
    }

    @Test
    public void testCount() {
        int count = DBHelper.with(mContext)
                .withTable(Student.class)
                .withWhere(Student.COLUMN_AGE + " > ?", 5)
                .applyCount();
        assertTrue(count > 0);
    }

    @Test
    public void testSearchById() {
        Student student = DBHelper.with(mContext)
                .withTable(Student.class)
                .applyFindById(1);
        assertTrue(student != null);
    }

    @Test
    public void testSearchAll(){
        List<Student> students = DBHelper.with(mContext)
                .withTable(Student.class)
                .applySearchAsList();
        assertTrue(students.size() > 0);
    }

    @Test
    public void testSearch(){
        List<Student> students = DBHelper.with(mContext)
                .withTable(Student.class)
                .withWhere(Student.COLUMN_AGE + ">?", 5)
                .applySearchAsList();
        assertTrue(students.size() > 0);
    }

    @Test
    public void testUpdate(){
        ContentValues values = new ContentValues();
        values.put(Student.COLUMN_NAME, "hello baby");

        int count = DBHelper.with(mContext)
                .withTable(Student.class)
                .withWhere(Student.COLUMN_AGE + "<?", 5)
                .applyUpdate(values);
        assertTrue(count > 0);
    }

    @Test
    public void testUpdateTable() {
        DBUtils dbUtils = DBHelper.with(mContext);
        Student student = dbUtils.withTable(Student.class).applyFindById(1);
        assertTrue(student != null);

        student.name = "testUpdateTable";
        int count = dbUtils.withTable(Student.class).applyUpdate(student);
        assertTrue(count > 0);
    }

    @Test
    public void testDeleteById(){
        int count = DBHelper.with(mContext).withTable(Student.class).applyDeleteById(1);
        assertTrue(count > 0);
    }

    @Test
    public void testDeleteByObject(){
        DBUtils dbUtils = DBHelper.with(mContext);
        Student student = dbUtils.withTable(Student.class).applyFindById(2);
        assertTrue(student != null);

        int count = dbUtils.withTable(Student.class).applyDelete(student);
        assertTrue(count > 0);
    }

    @Test
    public void testDelete() {
        int count = DBHelper.with(mContext).withTable(Student.class).withWhere(Student.COLUMN_AGE + ">=?", 9).applyDelete();
        assertTrue(count > 0);
    }

    @Test
    public void testExecuteBatchJobs(){
        BatchJobs jobs = new BatchJobs();
        Student student = new Student();
        student.name = "insert from batch job";
        student.age = 1;
        jobs.addInsertJob(student);

        // update with table object
        student = DBHelper.with(mContext).withTable(Student.class).applySearchFirst();
        student.name = "updated from batch job";
        jobs.addUpdateJob(Student.class, student);

        // update with id
        jobs.addUpdateJob(Student.class, student.id, student.toContentValues());

        // update with condition
        jobs.addUpdateJob(Student.class, student.toContentValues(), Student.COLUMN_AGE + "=?", 6);

        // delete with table object
        jobs.addDeleteJob(student);

        // delete with id
        jobs.addDeleteJob(Student.class, 7);

        // delete with condition
        jobs.addDeleteJob(Student.class, Student.COLUMN_AGE + "<?", 3);

        boolean success = DBHelper.with(mContext).applyBatchJobs(jobs);
        assertTrue(success);
    }
}
```

注意：在search时候调节式可以动态添加的，如：withGroupBy\(\),withHaving\(\),withDistinct\(\),withLimit\(\),withOrderBy等等。
