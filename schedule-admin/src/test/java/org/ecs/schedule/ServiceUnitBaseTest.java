package org.ecs.schedule;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ScheduleAdminMainApplication.class})
public class ServiceUnitBaseTest {

    @Test
    public void temp() {
        System.out.println("ServiceUnitBaseTest.temp");
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date()));
    }

}
