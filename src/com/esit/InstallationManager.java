package com.esit;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

import javax.naming.NamingException;
import javax.ws.rs.core.MultivaluedMap;

import org.json.JSONObject;

public class InstallationManager {
    private String saleId;
    private String installerId;
    private String installationDateTime;
    private String folderId;
    private String envelopeId;

    ConnectionManager conn;

    // Default constructor, do nothing
    public InstallationManager() {

    }

    // Constructor for POST requests
    public InstallationManager(MultivaluedMap<String, String> formParams) {
        this.setSaleId(formParams.get("saleId").get(0));
        this.setInstallerId(formParams.get("installerId").get(0));
        this.setInstallationDateTime(formParams.get("installationDateTime").get(0));
        this.setFolderId(formParams.get("folderId").get(0));
    }

    // Create installation
    public int create() {
        int result = 0;
        try {
            //Customer's create closed a connection, so we are creating a new one
            conn = new ConnectionManager();

            //TODO validate salesId and installerId before creating a new installation

            //create new installation object
            String newInstallationQuery = "INSERT INTO Installation ("
                    + "installer, sale, installationDateTime, status, folderId) "
                    + "VALUES(" + this.getInstallerId() + ", " + this.getSaleId() + ", '"
                    + this.getInstallationDateTime() + "', " + "'Scheduled', " + this.getFolderId() + ")";

            //execute new installation query
            result = conn.executeUpdate(newInstallationQuery);

            String updateSaleQuery = "UPDATE Sale set status='Finished' "
                    + "WHERE saleId=" + this.getSaleId();

            //execute update sale query
            result = conn.executeUpdate(updateSaleQuery);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //close the connection to the database
            conn.closeConnection();
        }

        return result;
    }
    
    // Get all installations
    public JSONObject getAllInstallations(int id) throws NamingException {
        JSONObject jsonObject = new JSONObject();
        try {
            String _query = "";
            EmployeeManager employeeMngr = new EmployeeManager();
            JSONObject jsonObj = new JSONObject();
            jsonObj = employeeMngr.getEmployeeById(id);
            JSONObject employee = jsonObj.getJSONObject("employee");
            String role = employee.getString("role");
            if(role != null && !role.isEmpty()) {
                if(role.equals("installer")) {
                    //create a query string
                    _query = "SELECT Installation.installationId, "
                            + "CONCAT(Customer.firstName, ' ', Customer.lastName) AS customerName, "
                            + "CONCAT(Employee.firstName, ' ', Employee.lastName) AS installerName, "
                            + "Program.programName, "
                            + "Address.street, "
                            + "DATE(Installation.installationDateTime) AS installationDate, "
                            + "Installation.status "
                            + "FROM Installation "
                            + "JOIN Sale ON Installation.sale = Sale.saleId "
                            + "JOIN Employee ON Installation.installer = Employee.employeeId "
                            + "JOIN Program ON Sale.program = Program.programId "
                            + "JOIN Property ON Sale.customer = Property.customer "
                            + "JOIN Address ON Property.address = Address.addressId "
                            + "JOIN Customer ON Sale.customer = Customer.customerId "
                            + "WHERE Installation.installer = " + id;
                }
                else {
                    //create a query string for managers and admins
                    _query = "SELECT Installation.installationId, "
                            + "CONCAT(Customer.firstName, ' ', Customer.lastName) AS customerName, "
                            + "CONCAT(Employee.firstName, ' ', Employee.lastName) AS installerName, "
                            + "Program.programName, "
                            + "Address.street, "
                            + "DATE(Installation.installationDateTime) AS installationDate, "
                            + "Installation.status "
                            + "FROM Installation "
                            + "JOIN Sale ON Installation.sale = Sale.saleId "
                            + "JOIN Employee ON Installation.installer = Employee.employeeId "
                            + "JOIN Program ON Sale.program = Program.programId "
                            + "JOIN Property ON Sale.customer = Property.customer "
                            + "JOIN Address ON Property.address = Address.addressId "
                            + "JOIN Customer ON Sale.customer = Customer.customerId";
                }
            }
            
            //create a new Query object
            conn = new ConnectionManager();
            
            //execute the query statement and get the ResultSet
            ResultSet resultSet = conn.executeQuery(_query);
            
            
            //creating an object to keep a collection of JSONs
            Collection<JSONObject> installations = new ArrayList<JSONObject>();

            // Iterating through the Results and filling the jsonObject
            while (resultSet.next()) {
              //creating a temporary JSON object and put there a data from the database
              JSONObject tempJson = new JSONObject();
              tempJson.put("installationNumber", resultSet.getString("installationId"));
              tempJson.put("customerName", resultSet.getString("customerName"));
              tempJson.put("installerName", resultSet.getString("installerName"));
              tempJson.put("product", resultSet.getString("programName"));
              tempJson.put("address", resultSet.getString("street"));
              tempJson.put("installationDate", resultSet.getString("installationDate"));
              tempJson.put("status", resultSet.getString("status"));
              installations.add(tempJson);
            }
            
            //creating a final JSON object
            jsonObject.put("installations", installations);

          } catch (Exception e) {
              e.printStackTrace();
          } finally {
              //close the connection to the database
              conn.closeConnection();
          }
        return jsonObject;
    }
    
    public int setEnvelopeId(int id, MultivaluedMap<String, String> formParams) {
        int result = 0;
        String envelopeId = formParams.get("envelopeId").get(0);
        System.out.println(envelopeId);
        try {

        	//getting a connection to the Database
            conn = new ConnectionManager();
            
            // Set the folderId
            this.setEnvelopeId(envelopeId);
            System.out.println("get: " + this.getEnvelopeId());
            
            //create new sale object
            String newSaleQuery = "UPDATE Installation SET "
                  + "envelopeId = "
                  + "'" + this.getEnvelopeId() + "' "
                  + "WHERE installationId = " + id;

            //execute new sale query
            result = conn.executeUpdate(newSaleQuery);

            //checking whether we created a Sale
            String getSaleQuery = "SELECT installationId, envelopeId "
                  + "FROM Installation "
                  + "WHERE installationId = " + id;

            ResultSet resultSet = conn.executeQuery(getSaleQuery);
            if(resultSet.next()) {
            	result = Integer.parseInt(resultSet.getString("installationId"));
          }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //close the connection to the database
            conn.closeConnection();
        }

        return result;
    }

    // Get all scheduled installations
    public JSONObject getAllScheduled(int id) throws NamingException {
        JSONObject jsonObject = new JSONObject();
        try {
            String _query = "";
            EmployeeManager employeeMngr = new EmployeeManager();
            JSONObject jsonObj = new JSONObject();
            jsonObj = employeeMngr.getEmployeeById(id);
            JSONObject employee = jsonObj.getJSONObject("employee");
            String role = employee.getString("role");
            if(role != null && !role.isEmpty()) {
                if(role.equals("installer")) {
                    //create a query string
                    _query = "SELECT Installation.installationId, "
                            + "CONCAT(Customer.firstName, ' ', Customer.lastName) AS customerName, "
                            + "CONCAT(Employee.firstName, ' ', Employee.lastName) AS installerName, "
                            + "Program.programName, "
                            + "Address.street, "
                            + "Installation.installationDateTime, "
                            + "Installation.status, "
                            + "Installation.folderId "
                            + "FROM Installation "
                            + "JOIN Sale ON Installation.sale = Sale.saleId "
                            + "JOIN Employee ON Installation.installer = Employee.employeeId "
                            + "JOIN Program ON Sale.program = Program.programId "
                            + "JOIN Property ON Sale.customer = Property.customer "
                            + "JOIN Address ON Property.address = Address.addressId "
                            + "JOIN Customer ON Sale.customer = Customer.customerId "
                            + "WHERE Installation.status != 'Cancelled' "
                            + "AND Installation.status != 'Completed' "
                            + "AND Installation.installer = " + id;
                } else {
                    _query = "SELECT Installation.installationId, "
                            + "CONCAT(Customer.firstName, ' ', Customer.lastName) AS customerName, "
                            + "CONCAT(Employee.firstName, ' ', Employee.lastName) AS installerName, "
                            + "Program.programName, "
                            + "Address.street, "
                            + "Installation.installationDateTime, "
                            + "Installation.status, "
                            + "Installation.folderId "
                            + "FROM Installation "
                            + "JOIN Sale ON Installation.sale = Sale.saleId "
                            + "JOIN Employee ON Installation.installer = Employee.employeeId "
                            + "JOIN Program ON Sale.program = Program.programId "
                            + "JOIN Property ON Sale.customer = Property.customer "
                            + "JOIN Address ON Property.address = Address.addressId "
                            + "JOIN Customer ON Sale.customer = Customer.customerId "
                            + "WHERE Installation.status != 'Cancelled' "
                            + "AND Installation.status != 'Completed' ";
                }
            }

            //create a new Query object
            conn = new ConnectionManager();

            //execute the query statement and get the ResultSet
            ResultSet resultSet = conn.executeQuery(_query);

            //creating an object to keep a collection of JSONs
            Collection<JSONObject> installations = new ArrayList<JSONObject>();

            // Iterating through the Results and filling the jsonObject
            while (resultSet.next()) {
              //creating a temporary JSON object and put there a data from the database
              JSONObject tempJson = new JSONObject();
              tempJson.put("installationNumber", resultSet.getString("installationId"));
              tempJson.put("customerName", resultSet.getString("customerName"));
              tempJson.put("installerName", resultSet.getString("installerName"));
              tempJson.put("product", resultSet.getString("programName"));
              tempJson.put("address", resultSet.getString("street"));
              tempJson.put("installationDateTime", resultSet.getString("installationDateTime"));
              tempJson.put("status", resultSet.getString("status"));
              tempJson.put("folderId", resultSet.getString("folderId"));
              installations.add(tempJson);
            }

            //creating a final JSON object
            jsonObject.put("installations", installations);

          } catch (Exception e) {
              e.printStackTrace();
          } finally {
              //close the connection to the database
              conn.closeConnection();
          }
        return jsonObject;
    }
    
    // Get installation by Id
    public JSONObject getInstallationById(int id) throws NamingException {
        JSONObject jsonObject = new JSONObject();
        try {
            //create a query string
            String _query = "SELECT Installation.installationId, " 
                + "Customer.firstName, "
                + "Customer.lastName, "
                + "CONCAT(Employee.firstName, ' ', Employee.lastName) AS installerName, "
                + "Employee.employeeId, "
                + "Program.programName, "
                + "Address.street, "
                + "Address.unit, "
                + "Address.city, "
                + "Address.province, "
                + "Address.postalCode, "
                + "Customer.enbridgeNum, "
                + "Customer.email, "
                + "Customer.homePhone, "
                + "Customer.cellPhone, "
                + "Property.sqFootage , "
                + "Property.bathrooms , "
                + "Property.residents , "
                + "Property.hasPool, "
                + "Installation.installationDateTime "
                + "FROM Installation "
                + "JOIN Sale ON Installation.sale = Sale.saleId "
                + "JOIN Employee ON Installation.installer = Employee.employeeId "
                + "JOIN Program ON Sale.program = Program.programId "
                + "JOIN Property ON Sale.customer = Property.customer "
                + "JOIN Address ON Property.address = Address.addressId "
                + "JOIN Customer ON Sale.customer = Customer.customerId "
                + "WHERE Installation.installationId = " + id;

            //create a new Query object
            conn = new ConnectionManager();
            
            //execute the query statement and get the ResultSet
            ResultSet resultSet = conn.executeQuery(_query);
            
            //creating a temporary JSON object and put there a data from the database
            JSONObject installation = new JSONObject();

            // If there are results fill the jsonObject
            if (resultSet.next()) {
              installation.put("installationNumber", resultSet.getString("installationId"));
              installation.put("customerFirstName", resultSet.getString("firstName"));
              installation.put("customerLastName", resultSet.getString("lastName"));
              installation.put("installerId", resultSet.getString("employeeId"));
              installation.put("installerName", resultSet.getString("installerName"));
              installation.put("product", resultSet.getString("programName"));
              installation.put("address", resultSet.getString("street"));
              installation.put("unit", resultSet.getString("unit"));
              installation.put("city", resultSet.getString("city"));
              installation.put("province", resultSet.getString("province"));
              installation.put("postalCode", resultSet.getString("postalCode"));
              installation.put("enbridgeNum", resultSet.getString("enbridgeNum"));
              installation.put("homePhone", resultSet.getString("homePhone"));
              installation.put("cellPhone", resultSet.getString("cellPhone"));
              installation.put("email", resultSet.getString("email"));
              installation.put("sqFootage", resultSet.getString("sqFootage"));
              installation.put("bathrooms", resultSet.getString("bathrooms"));
              installation.put("residents", resultSet.getString("residents"));
              installation.put("hasPool", resultSet.getString("hasPool"));
              installation.put("installationDateTime", resultSet.getString("installationDateTime"));
            }
            
            //creating a final JSON object
            jsonObject.put("installation", installation);

          } catch (Exception e) {
              e.printStackTrace();
          } finally {
              //close the connection to the database
              conn.closeConnection();
          }
        return jsonObject;
    }

    
    public String getSaleId() {
        return saleId;
    }

    public void setSaleId(String saleId) {
        this.saleId = saleId;
    }

    public String getInstallerId() {
        return installerId;
    }

    public void setInstallerId(String installerId) {
        this.installerId = installerId;
    }

    public String getInstallationDateTime() {
        return installationDateTime;
    }

    public void setInstallationDateTime(String installationDateTime) {
        this.installationDateTime = installationDateTime;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

	public String getEnvelopeId() {
		return envelopeId;
	}

	public void setEnvelopeId(String envelopeId) {
		this.envelopeId = envelopeId;
	}
}
