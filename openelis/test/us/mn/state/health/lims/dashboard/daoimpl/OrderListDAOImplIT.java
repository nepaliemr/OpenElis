package us.mn.state.health.lims.dashboard.daoimpl;

import junit.framework.Assert;
import org.bahmni.feed.openelis.IT;
import org.junit.Before;
import org.junit.Ignore;
import us.mn.state.health.lims.analysis.daoimpl.AnalysisDAOImpl;
import us.mn.state.health.lims.analysis.valueholder.Analysis;
import us.mn.state.health.lims.analyte.daoimpl.AnalyteDAOImpl;
import us.mn.state.health.lims.analyte.valueholder.Analyte;
import us.mn.state.health.lims.common.action.IActionConstants;
import us.mn.state.health.lims.common.util.DateUtil;
import us.mn.state.health.lims.dashboard.valueholder.Order;
import us.mn.state.health.lims.patient.daoimpl.PatientDAOImpl;
import us.mn.state.health.lims.patient.valueholder.Patient;
import us.mn.state.health.lims.patientidentity.daoimpl.PatientIdentityDAOImpl;
import us.mn.state.health.lims.patientidentity.valueholder.PatientIdentity;
import us.mn.state.health.lims.patientidentitytype.util.PatientIdentityTypeMap;
import us.mn.state.health.lims.person.daoimpl.PersonDAOImpl;
import us.mn.state.health.lims.person.valueholder.Person;
import us.mn.state.health.lims.result.daoimpl.ResultDAOImpl;
import us.mn.state.health.lims.result.valueholder.Result;
import us.mn.state.health.lims.sample.daoimpl.SampleDAOImpl;
import us.mn.state.health.lims.sample.valueholder.Sample;
import us.mn.state.health.lims.samplehuman.daoimpl.SampleHumanDAOImpl;
import us.mn.state.health.lims.samplehuman.valueholder.SampleHuman;
import us.mn.state.health.lims.sampleitem.daoimpl.SampleItemDAOImpl;
import us.mn.state.health.lims.sampleitem.valueholder.SampleItem;
import us.mn.state.health.lims.samplesource.daoimpl.SampleSourceDAOImpl;
import us.mn.state.health.lims.samplesource.valueholder.SampleSource;
import us.mn.state.health.lims.statusofsample.util.StatusOfSampleUtil;
import us.mn.state.health.lims.test.dao.TestSectionDAO;
import us.mn.state.health.lims.test.daoimpl.TestDAOImpl;
import us.mn.state.health.lims.test.daoimpl.TestSectionDAOImpl;
import us.mn.state.health.lims.test.valueholder.Test;
import us.mn.state.health.lims.test.valueholder.TestSection;
import us.mn.state.health.lims.testanalyte.daoimpl.TestAnalyteDAOImpl;
import us.mn.state.health.lims.testanalyte.valueholder.TestAnalyte;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static us.mn.state.health.lims.statusofsample.util.StatusOfSampleUtil.AnalysisStatus.*;

public class OrderListDAOImplIT extends IT {

    private String accessionNumber;
    private String patientIdentityData;
    private String firstName;
    private String lastName;
    private OrderListDAOImpl orderListDAO;

    @Before
    public void setUp() throws Exception {
        accessionNumber = "05082013-001";
        patientIdentityData = "TEST1234567";
        firstName = "Some";
        lastName = "One";
        orderListDAO = new OrderListDAOImpl();
    }

    @org.junit.Test
    @Ignore
    public void getAllInProgress_shouldReturnAllOrdersWhichAreInProgress() {
        Sample sample = createSample(accessionNumber);
        Patient patient = createPatient(firstName, lastName, patientIdentityData);
        createSampleHuman(sample, patient);
        SampleItem sampleItem = createSampleItem(sample);


        TestDAOImpl testDAO = createTests("SampleTest1", "SampleTest2", "SampleTest3", "SampleTest4");

        List<Test> allTests = testDAO.getAllTests(true);

        Analysis analysis_1 = createAnalysis(sampleItem, TechnicalAcceptance, "Hematology", allTests.get(0));
        Analysis analysis_2 = createAnalysis(sampleItem, NotStarted, "Hematology", allTests.get(1));

        Analysis analysis_3 = createAnalysis(sampleItem, NotStarted, "Hematology", allTests.get(2));
        createResult(analysis_3);

        Analysis analysis_4 = createAnalysis(sampleItem, ReferedOut, "Hematology", allTests.get(3));

        List<Order> inProgress = orderListDAO.getAllInProgress();

        Assert.assertTrue(inProgress.contains(new Order(accessionNumber, patientIdentityData, firstName, lastName, sample.getSampleSource().getName(), 2, 3)));
    }

    @org.junit.Test
    public void getAllCompleted_shouldReturnAllOrdersWhichAreCompletedBefore24Hours() {
        Sample sample = createSample(accessionNumber);
        Patient patient = createPatient(firstName, lastName, patientIdentityData);
        createSampleHuman(sample, patient);
        SampleItem sampleItem = createSampleItem(sample);

        TestDAOImpl testDAO = createTests("SampleTest1", "SampleTest2", "SampleTest3", "SampleTest4");

        List<Test> allTests = testDAO.getAllTests(true);

        createAnalysis(sampleItem, StatusOfSampleUtil.AnalysisStatus.Finalized, "Hematology", allTests.get(0));
        createAnalysis(sampleItem, StatusOfSampleUtil.AnalysisStatus.Canceled, "Hematology", allTests.get(1));

        List<Order> completedOrders = orderListDAO.getAllCompletedBefore24Hours();

        Assert.assertTrue(completedOrders.contains(new Order(accessionNumber, patientIdentityData, firstName, lastName, sample.getSampleSource().getName(), -1, -1)));
    }


    private TestDAOImpl createTests(String... samples) {
        TestDAOImpl testDAO = new TestDAOImpl();
        for (String sample : samples) {
            createATest(testDAO, sample);
        }
        return testDAO;
    }

    private Test createATest(TestDAOImpl testDAO, String testName) {
        Test test = createTest(testName);
        testDAO.insertData(test);
        TestAnalyteDAOImpl testAnalyteDAO = new TestAnalyteDAOImpl();
        testAnalyteDAO.insertData(createTestAnalyte(test, new AnalyteDAOImpl().readAnalyte("1")));
        return test;
    }

    private TestAnalyte createTestAnalyte(Test test, Analyte analyte) {
        TestAnalyte testAnalyte = new TestAnalyte();
        testAnalyte.setTest(test);
        testAnalyte.setAnalyte(analyte);
        testAnalyte.setSysUserId("1");
        testAnalyte.setResultGroup("1");
        return testAnalyte;
    }

    private Test createTest(String testName) {
        Test test = new Test();
        test.setTestName(testName);
        test.setDescription(testName);
        test.setIsActive(IActionConstants.YES);
        test.setSortOrder("1");
        test.setOrderable(Boolean.TRUE);
        test.setSysUserId("1");
        return test;
    }

    private Result createResult(Analysis analysis) {
        Result result = new Result();
        result.setSysUserId("1");
        result.setAnalysis(analysis);
        result.setValue("10");
        new ResultDAOImpl().insertData(result);
        return result;
    }

    private Patient createPatient(String firstName, String lastName, String patientIdentityData) {
        Person person = new Person();
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setSysUserId("1");
        new PersonDAOImpl().insertData(person);

        Patient patient = new Patient();
        patient.setPerson(person);
        patient.setGender("M");
        patient.setBirthDate(DateUtil.getNowAsTimestamp());
        patient.setSysUserId("1");
        patient.setLastupdated(DateUtil.getNowAsTimestamp());
        new PatientDAOImpl().insertData(patient);

        PatientIdentity patientIdentity = new PatientIdentity();
        patientIdentity.setPatientId(patient.getId());
        patientIdentity.setIdentityTypeId(PatientIdentityTypeMap.getInstance().getIDForType("ST"));
        patientIdentity.setIdentityData(patientIdentityData);
        patientIdentity.setSysUserId("1");
        new PatientIdentityDAOImpl().insertData(patientIdentity);

        return patient;
    }

    private SampleHuman createSampleHuman(Sample sample, Patient patient) {
        SampleHuman sampleHuman = new SampleHuman();
        sampleHuman.setPatientId(patient.getId());
        sampleHuman.setSampleId(sample.getId());
        sampleHuman.setSysUserId("1");

        new SampleHumanDAOImpl().insertData(sampleHuman);
        return sampleHuman;
    }


    private Analysis createAnalysis(SampleItem sampleItem, StatusOfSampleUtil.AnalysisStatus analysisStatus, String testSectionName, Test test) {
        TestSectionDAO testSectionDAO = new TestSectionDAOImpl();
        TestSection testSection = testSectionDAO.getTestSectionByName(testSectionName);


        Analysis analysis = new Analysis();
        analysis.setSampleItem(sampleItem);
        analysis.setAnalysisType(IActionConstants.ANALYSIS_TYPE_MANUAL);
        analysis.setStatusId(StatusOfSampleUtil.getStatusID(analysisStatus));
        analysis.setTest(test);
        analysis.setSysUserId("1");
        analysis.setTestSection(testSection);
        new AnalysisDAOImpl().insertData(analysis, false);

        return analysis;
    }

    private SampleItem createSampleItem(Sample startedSample) {
        SampleItem enteredSampleItem = new SampleItem();
        enteredSampleItem.setSample(startedSample);
        enteredSampleItem.setStatusId(StatusOfSampleUtil.getStatusID(StatusOfSampleUtil.SampleStatus.Entered));
        enteredSampleItem.setSortOrder("1");
        enteredSampleItem.setSysUserId("1");
        new SampleItemDAOImpl().insertData(enteredSampleItem);
        return enteredSampleItem;
    }

    private Sample createSample(String accessionNumber) {
        List<SampleSource> sampleSources = new SampleSourceDAOImpl().getAll();
        Sample sample = new Sample();
        sample.setAccessionNumber(accessionNumber);
        sample.setStatusId(StatusOfSampleUtil.getStatusID(StatusOfSampleUtil.OrderStatus.Started));
        sample.setEnteredDate(DateUtil.convertStringDateToSqlDate(new SimpleDateFormat("dd/MM/yyyy").format(new Date())));
        sample.setReceivedTimestamp(DateUtil.convertStringDateToTimestamp("01/01/2001 00:00"));
        sample.setSampleSource(sampleSources.get(0));
        sample.setSysUserId("1");
        new SampleDAOImpl().insertDataWithAccessionNumber(sample);
        return sample;
    }

}