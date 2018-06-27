package com.feiyan.lightdao;

import android.content.ContentValues;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.feiyan.lightdao.tables.Relation;
import com.feiyan.lightdao.tables.Student;
import com.feiyan.lightdao.tables.Teacher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class DBTester {
    private Context mContext;

    @Before
    public void before(){
        mContext = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testSave(){
        Teacher teacher = new Teacher();
        teacher.name = "王老师";
        long teacherId = DBHelper.with(mContext).save(teacher);

        Student student = new Student();
        student.teacherId = teacherId;
        student.name = "小学生";
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
            student.teacherId = 1; // 假设都是ID为1的教师的学生
            students.add(student);
        }
        int count = DBHelper.with(mContext).saveAll(students);
        assertTrue(count == students.size());
    }

    @Test
    public void testCount() {
        int count = DBHelper.with(mContext)
                .withTable(Student.class)
                .withWhere("age > ?", 5)
                .applyCount();
        assertTrue(count > 0);
    }

    @Test
    public void testSearchById() {
        Student student = DBHelper.with(mContext)
                .withTable(Student.class)
                .applySearchById(1);
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
                .withWhere("age>?", 5)
                .applySearchAsList();
        assertTrue(students.size() > 0);
    }

    @Test
    public void testUpdate(){
        ContentValues values = new ContentValues();
        values.put("name", "hello baby");

        int count = DBHelper.with(mContext)
                .withTable(Student.class)
                .withWhere("age<?", 5)
                .applyUpdate(values);
        assertTrue(count > 0);
    }

    @Test
    public void testUpdateTable() {
        DBUtils dbUtils = DBHelper.with(mContext);
        Student student = dbUtils.withTable(Student.class).applySearchById(1);
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
        Student student = dbUtils.withTable(Student.class).applySearchById(2);
        assertTrue(student != null);

        int count = dbUtils.withTable(Student.class).applyDelete(student);
        assertTrue(count > 0);
    }

    @Test
    public void testDelete() {
        int count = DBHelper.with(mContext).withTable(Student.class).withWhere("age>=?", 9).applyDelete();
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
        jobs.addUpdateJob(Student.class, student.toContentValues(), "age=?", 6);

        // delete with table object
        jobs.addDeleteJob(student);

        // delete with id
        jobs.addDeleteJob(Student.class, 7);

        // delete with condition
        jobs.addDeleteJob(Student.class, "age<?", 3);

        boolean success = DBHelper.with(mContext).applyBatchJobs(jobs);
        assertTrue(success);
    }

    @Test
    public void testCrossTableQuery(){
        List<Relation> list = DBHelper.with(mContext)
                .withQuery(Relation.class)
                .applySearchAsList();
        System.out.println(list.size());
    }
}
