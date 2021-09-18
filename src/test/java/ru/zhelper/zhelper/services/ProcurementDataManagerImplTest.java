package ru.zhelper.zhelper.services;

import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import ru.zhelper.zhelper.models.Procurement;
import ru.zhelper.zhelper.repository.ProcurementRepo;
import ru.zhelper.zhelper.services.exceptions.DataManagerException;

@ActiveProfiles("test")
@SpringBootTest
@Sql(scripts = {"/sql/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProcurementDataManagerImplTest {

    private ProcurementDataManagerImpl procurementDataManager;

    @Autowired
    private ProcurementRepo repository;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcurementDataManagerImplTest.class);

    private final static String MY_UIN = "202320000012100777";
    private final static Long ID_NON_EXISTING_PROCUREMENT = 8765L;
    private final static int FZ_NUMBER_OF_SECOND_PROCUREMENT = 44;
    private final static int FZ_NUMBER_OF_SAVED_PROCUREMENT = 615;
    private final static String EXCEPTION_NOT_RECEIVED = "Exception not received: ";

	Pageable firstPageWithFiveElements = PageRequest.of(0, 5);
	// 5 = page size, 20 = page number
	Pageable secondPageWithFiveElements = PageRequest.of(5, 20);

    @BeforeAll
    void init() {
        procurementDataManager = new ProcurementDataManagerImpl();
        procurementDataManager.setRepository(repository);

    }

    @Test
    @Transactional
    void testSave() {
        Procurement procurement = new Procurement();
        procurement.setContractPrice(BigDecimal.TEN);
        procurement.setUin("ABC124z34");
        procurement.setFzNumber(FZ_NUMBER_OF_SAVED_PROCUREMENT);

        Procurement saved = procurementDataManager.save(procurement);

        // Check the saved data
        Assertions.assertEquals(saved.getContractPrice(), procurement.getContractPrice());
        Assertions.assertEquals(saved.getUin(), procurement.getUin());
        Assertions.assertEquals(saved.getFzNumber(), procurement.getFzNumber());
    }

    @Test
    @Transactional
    void testGetOfUpdate() {
        Page<Procurement> allProcurements = procurementDataManager.loadAll(firstPageWithFiveElements);

        Procurement second = procurementDataManager.loadById(allProcurements.get().findAny().get().getId());
        Assertions.assertEquals(FZ_NUMBER_OF_SAVED_PROCUREMENT, second.getFzNumber());

        second.setUin(MY_UIN);
        Procurement updated = procurementDataManager.save(second);

        Assertions.assertEquals(MY_UIN, updated.getUin());
        Assertions.assertEquals(FZ_NUMBER_OF_SAVED_PROCUREMENT, updated.getFzNumber());
    }

    @Test
    @Transactional
    void testLoadProcurementsByFzNumber() {
        Assertions.assertEquals(1,
                procurementDataManager.loadListByFzNumber(FZ_NUMBER_OF_SECOND_PROCUREMENT).size());
    }

    /**
     * First call loadProcurementsByFzNumber() to receive 1 result.
     * Then test deletion of object (not by id).
     * After deletion call loadProcurementsByFzNumber() again.
     * Search should deliver 0 results since object was deleted.
     */
    @Test
    @Transactional
    void testDeleteAndCountRemaining() {
        List<Procurement> foundList = procurementDataManager.loadListByFzNumber(FZ_NUMBER_OF_SECOND_PROCUREMENT);
        Assertions.assertEquals(1, foundList.size());
        procurementDataManager.delete(foundList.get(0));
        Assertions.assertEquals(0,
                procurementDataManager.loadListByFzNumber(FZ_NUMBER_OF_SECOND_PROCUREMENT).size());
    }

    @Test
    @Transactional
    void testDeleteById() {
        Page<Procurement> allProcurements = procurementDataManager.loadAll(firstPageWithFiveElements);
        Assertions.assertEquals(2, allProcurements.stream().count());
        allProcurements.stream().forEach(System.out::println);

        procurementDataManager.deleteById(allProcurements.get().findAny().get().getId());

        // Assertion - is it really deleted?
        Assertions.assertThrows(JpaObjectRetrievalFailureException.class,
                () -> procurementDataManager.loadById(allProcurements.get().findAny().get().getId()));
    }

    @Test
    @Transactional
    void testLoadProcurementWithIdNull() {
        try {
            procurementDataManager.loadById(null);
        } catch (DataManagerException dataMgrExc) {
            if (!DataManagerException.COULD_NOT_LOAD_PROCUREMENT_NULL_DATA.equals(dataMgrExc.getMessage())) {
                fail(EXCEPTION_NOT_RECEIVED + DataManagerException.COULD_NOT_LOAD_PROCUREMENT_NULL_DATA);
            }
        }
    }

    @Test
    @Transactional
    void testDeleteNonExisting() {
        try {
            procurementDataManager.deleteById(ID_NON_EXISTING_PROCUREMENT);
        } catch (DataManagerException dataMgrExc) {
            String expectedMessage = String.format(
                    DataManagerException.NON_EXISTING_LOAD_OR_DELETE_EXCEPTION, ID_NON_EXISTING_PROCUREMENT);
            if (!expectedMessage.equals(dataMgrExc.getMessage())) {
                fail(EXCEPTION_NOT_RECEIVED + expectedMessage);
            }
        }
    }

    @Test
    @Transactional
    void testLoadAll() {
        Page<Procurement> allProcurements = procurementDataManager.loadAll(firstPageWithFiveElements);
        Assertions.assertEquals(2, allProcurements.stream().count());
    }
    
    @Test
    @Transactional
    void testLoadByIdList() {
    	Page<Procurement> allProcurements = procurementDataManager.loadAll(firstPageWithFiveElements);
        Assertions.assertEquals(2, allProcurements.stream().count());
        // Get a collection of all the ids
        List<Long> ids = allProcurements.stream()
                                 .map(Procurement::getId).collect(Collectors.toList());
        if (LOGGER.isInfoEnabled()) {
        	LOGGER.info("----> ids: {}", ids);
        }

        Page<Procurement> result = procurementDataManager.loadByIdList(ids,
        		firstPageWithFiveElements);
        Assertions.assertEquals(2, result.stream().count());
    }
    
    @Test
    @Transactional
    void testLoadCreatedBeforeDate() {
    	Page<Procurement> result = procurementDataManager.loadCreatedBeforeDate(
    			LocalDate.of(2021, 2, 1), firstPageWithFiveElements);
        Assertions.assertEquals(1, result.stream().count());
    }
}