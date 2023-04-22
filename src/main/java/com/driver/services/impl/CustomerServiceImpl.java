package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Customer customer=customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query

		List<Driver> drivers=driverRepository2.findAll();

		TripBooking trip=new TripBooking();

		boolean cabAvailable=false;

		for(Driver driver: drivers){
			if(driver.getCab().isAvailable()==true){
				// Making the available false
				driver.getCab().setAvailable(false);

				// setting the parameters of trip
				trip.setStatus(TripStatus.CONFIRMED);
				trip.setFromLocation(fromLocation);
				trip.setToLocation(toLocation);
				trip.setDistanceInKm(distanceInKm);
				trip.setBill(driver.getCab().getPerKmRate()*distanceInKm);

				// Setting the Parameters of customer
				Customer customer=customerRepository2.findById(customerId).get();
				List<TripBooking> tripBookings=customer.getTripBookingList();
				tripBookings.add(trip);
				customer.setTripBookingList(tripBookings);

				//Setting the parameter of Driver
				List<TripBooking> tripBookings2=driver.getTripBookingList();
				tripBookings2.add(trip);
				driver.setTripBookingList(tripBookings2);

				trip.setCustomer(customer);
				trip.setDriver(driver);

				customerRepository2.save(customer);
				driverRepository2.save(driver);
				cabAvailable=true;

				break;  // no need to iterate more
			}
		}

		if(!cabAvailable) throw new Exception("No cab available!");
		else return trip;

	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking currTrip=tripBookingRepository2.findById(tripId).get();
		currTrip.setStatus(TripStatus.CANCELED);
		currTrip.setBill(0);
		currTrip.getDriver().getCab().setAvailable(true);

		tripBookingRepository2.save(currTrip);

	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.COMPLETED);
		tripBooking.setBill(tripBooking.getDriver().getCab().getPerKmRate()*tripBooking.getDistanceInKm());

		tripBooking.getDriver().getCab().setAvailable(true); // Changing the available parameter of Cab

		tripBookingRepository2.save(tripBooking);
	}
}
