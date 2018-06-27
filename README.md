## light-dao 

一个非常轻量级别的SQLite ORM，通过使用Java注解和Java反射实现.

## 想法起源：
最初的萌芽来自于Android Email App内部一个叫[EmailContent](https://github.com/android/platform_packages_apps_email/blob/552ef93f45b6f818bb269920c309741c51e62b1e/emailcommon/src/com/android/emailcommon/provider/EmailContent.java)的class定义。每个Table类都手动实现toContentValues()和restore(Cursor cursor)，这样可以避免重复通过Cursor拼装表对象。随后自己就想写了一个Utils类似的工具类类来提供一系列增删改查的API，当然这些API的操作对象都是table对象，随着迭代慢慢衍化如今更加友好的light-dao了。

## 下面描述下如何使用此light-dao：
### 1. 得定义一个继承BaseDBHelper的DBHelper，类似Android SDK的做法:

```java
public class DBHelper extends BaseDBHelper {
    private static final String DATABASE_NAME = "school.db";
    private static final int VERSION = 1;

    @SuppressLint("StaticFieldLeak")
    private static DBHelper sSingleton;

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, VERSION);
    }

    private static DBHelper getSingleton(Context context) {
        if (sSingleton == null) {
            synchronized (DBHelper.class) {
                sSingleton = new DBHelper(context.getApplicationContext());
            }
        }
        return sSingleton;
    }

    public static DBUtils with(Context context) {
        return DBUtils.create(getSingleton(context));
    }

    /**
     * all table classes should configured here
     *
     * @param tableClasses table classes
     */
    @Override
    protected void onClassLoad(List<Class<? extends Entity>> tableClasses) {
        tableClasses.add(Student.class);
        tableClasses.add(Teacher.class);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);

        /* upgrade db version by version
        if (oldVersion < VERSION) {
            SQL sql = SQLBuilder.buildTableCreateSQL(Student.class);
            db.execSQL(sql.getSql());
        }
        */
    }
}
```

### 2. 然后，定义你的各种表的类，如下为学生表和老师表，且学生表里有老师表ID的外键：

```java
@Table("teacher")
public class Teacher extends Entity {
    @Column(name = "name", notnull = true)
    public String name;
}  

@Table("student")
public class Student extends Entity {
    @Foreign(Teacher.class)
    @Column(name = "teacher_id", notnull = true)
    public long teacherId;

    @Column(name = "name", notnull = true)
    public String name;

    @Column(name = "age", notnull = true)
    public Integer age;
}
```

### 3. 最后就可以通过lightdao进行常见的数据库增删改查了：
#### 3.1 单个保存

```java
Teacher teacher = new Teacher();
teacher.name = "王老师";
long teacherId = DBHelper.with(mContext).save(teacher);

// 老师和学生关系是一对多，因此学生表中有老师表的ID作为外键
Student student = new Student();
student.teacherId = teacherId;
student.name = "小学生";
student.age = 20;
long id = DBHelper.with(mContext).save(student);
assertTrue(id > 0);
```

#### 3.2 批量保存

```java
List<Student> students = new ArrayList<>();
for (int i = 0; i < 10; i++) {
    Student student = new Student();
    student.name = "name " + i;
    student.age = i;
    student.teacherId = 1; // 假设都是ID为1的教师的学生
    students.add(student);
}
int count = DBHelper.with(mContext).saveAll(students);
assertTrue(count == students.size());
```

#### 3.3 数量查询

```java
int count = DBHelper.with(mContext)
    .withTable(Student.class)
    .withWhere("age > ?", 5)
    .applyCount();
assertTrue(count > 0);
```        

#### 3.5 根据主键ID查找

```java
Student student = DBHelper.with(mContext)
    .withTable(Student.class)
    .applySearchById(1);
assertTrue(student != null);
```

#### 3.6 查询所有并以list返回结果

```java
List<Student> students = DBHelper.with(mContext)
    .withTable(Student.class)
    .applySearchAsList();
assertTrue(students.size() > 0);
```

#### 3.7 带有条件查询并以list返回结果

```java
// 类似的还有很多其他以“with”开头的API，如：
// withColumns: 只查询指定的column
// withGroupBy: 查询分组
// withHaving: 分组后的条件筛选
// withOrderBy: 排序控制
// withLimit: 分页控制
// withDistinct: 查询去重

List<Student> students = DBHelper.with(mContext)
    .withTable(Student.class)
    .withWhere("age>?", 5)
    .applySearchAsList();
assertTrue(students.size() > 0);
```

#### 3.8 更新部分字段

```java
ContentValues values = new ContentValues();
values.put("name", "hello baby");

int count = DBHelper.with(mContext)
        .withTable(Student.class)
        .withWhere("age<?", 5)
        .applyUpdate(values);
assertTrue(count > 0);
```

#### 3.9 根据对象更新

```java
DBUtils dbUtils = DBHelper.with(mContext);
Student student = dbUtils.withTable(Student.class).applySearchById(1);
assertTrue(student != null);

student.name = "testUpdateTable";
int count = dbUtils.withTable(Student.class).applyUpdate(student);
assertTrue(count > 0);
```

#### 4.0 根据主键ID删除

```java
int count = DBHelper.with(mContext).withTable(Student.class).applyDeleteById(1);
assertTrue(count > 0);
```

#### 4.1 删除指定的对象

```java
DBUtils dbUtils = DBHelper.with(mContext);
Student student = dbUtils.withTable(Student.class).applySearchById(2);
assertTrue(student != null);

int count = dbUtils.withTable(Student.class).applyDelete(student);
assertTrue(count > 0);
```

#### 4.2 根据条件删除

```java
int count = DBHelper.with(mContext).withTable(Student.class).withWhere("age>=?", 9).applyDelete();
assertTrue(count > 0);
```

#### 4.3 批处理（数据库事务）

```java
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
jobs.addUpdateJob(Student.class, student.toContentValues(), "age=?", 6);

// delete with table object
jobs.addDeleteJob(student);

// delete with id
jobs.addDeleteJob(Student.class, 7);

// delete with condition
jobs.addDeleteJob(Student.class, "age<?", 3);

boolean success = DBHelper.with(mContext).applyBatchJobs(jobs);
assertTrue(success);
```

#### 4.4 跨表查询

```java
// 因为跨表查询的结果来自于多个表，所以得重新定义返回结果的对象，并通过aliasName指定此字段来自于哪个表中的哪个字段
@InnerJoin(@InnerJoinItem(firstTable = "student", firstColumn = "_id", secondTable ="teacher", secondColumn = "_id"))
public class Relation extends Query {
    @Column(name = "teacher_id", aliasName = "student._id as teacher_id")
    public long teacherId;

    @Column(name = "teacher_name", aliasName = "teacher.name as teacher_name")
    public String teacherName;

    @Column(name = "student_id", aliasName = "student._id as student_id")
    public long studentId;

    @Column(name = "student_name", aliasName = "student.name as student_name")
    public String studentName;

    @Column(name = "age")
    public int studentAge;
}

// withColumns: 指定查询要返回的字段定义类
// withTableNames: 指定要跨表查询的表名
List<Relation> list = DBHelper.with(mContext)
    .withColumns(Relation.class)
    .withTableNames("student", "teacher")
    .withWhere("teacher_id=student._id")
    .applySearchAsList();

System.out.println(list.size());
```

>因为跨表查询需要指定便于表之间的外键关联关系，所以需要借助@InnerJoin, @CrossJoin, @LeftJoin, @RightJoin, @NaturalJoin描述他们的关联关系。其中内连接因为支持多表以上连接，所以@InnerJoin的参数是一个数组(@InnerJoinItem)，每个item描述其中两张表之间的外键关联关系。