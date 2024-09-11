package edu.duke.delivery;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ch.qos.logback.core.status.Status;
import edu.duke.adtg.domain.Assessment;
import edu.duke.adtg.domain.Course;
import edu.duke.adtg.domain.CourseDAO;
import edu.duke.adtg.domain.DAOConn;
import edu.duke.adtg.domain.Delivery;
import edu.duke.adtg.domain.DeliveryDAO;
import edu.duke.adtg.domain.EnrollmentDAO;
import edu.duke.adtg.domain.Grade;
import edu.duke.adtg.domain.GradeDAO;
import edu.duke.adtg.domain.Section;
import edu.duke.adtg.domain.Student;
import edu.duke.adtg.domain.User;

@SpringBootApplication
public class DeliveryApplication {

	public static void main(String[] args) {
		RepoFuncLib funcs = new RepoFuncLib();

		DAOConn conn = new DAOConn();
		DeliveryDAO deliveryDAO = new DeliveryDAO(conn);

		//check all rows in delivery
		List<Delivery> deliveries = deliveryDAO.getDelivery();
		for(Delivery delivery:deliveries){
			// if(delivery.getStatus().equals(edu.duke.adtg.domain.Status.INIT)){
			// 	System.out.println(delivery.getAssessment().getSubject()+delivery.getAssessment().getNumber()+delivery.getStudent().getNetId());
			// }

			try{
				if(delivery.getStatus().equals(edu.duke.adtg.domain.Status.INIT)){
					funcs.initDelivery(delivery);
					System.out.println(delivery.getStudent().getNetId()+" "+delivery.getStatus().toString()+" "+delivery.getLog());
					deliveryDAO.update(delivery);
				}
				else if(delivery.getStatus().equals(edu.duke.adtg.domain.Status.DELIVER)){
					funcs.deliverDelivery(delivery);
					System.out.println(delivery.getStudent().getNetId()+" "+delivery.getStatus().toString()+" "+delivery.getLog());
					deliveryDAO.update(delivery);
				}

			}
			catch(Exception e){
				System.err.println(e.getMessage());
				delivery.setStatus(edu.duke.adtg.domain.Status.ERROR);
				delivery.setLog(e.getMessage());
				deliveryDAO.update(delivery);
				e.getStackTrace();
			}
		}

		
	}

}
