package jp.co.metateam.library.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Date;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.repository.AccountRepository;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.values.RentalStatus;

@Service
public class RentalManageService {

    private final AccountRepository accountRepository;
    private final RentalManageRepository rentalManageRepository;
    private final StockRepository stockRepository;

     @Autowired
    public RentalManageService(
        AccountRepository accountRepository,
        RentalManageRepository rentalManageRepository,
        StockRepository stockRepository
    ) {
        this.accountRepository = accountRepository;
        this.rentalManageRepository = rentalManageRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public List <RentalManage> findAll() {
        List <RentalManage> rentalManageList = this.rentalManageRepository.findAll();

        return rentalManageList;
    }

    @Transactional
    public RentalManage findById(Long id) {
        return this.rentalManageRepository.findById(id).orElse(null);
    }


    @Transactional
    public Long  otherReservations(String stock_id,Long id){
        return this.rentalManageRepository.otherReservations(stock_id,id);
    }

    
    @Transactional
    public Long otherDates(String stock_id,Long id,Date expectedReturnOn,Date expectedRentalOn){
        return this.rentalManageRepository.otherDates(stock_id,id,expectedReturnOn,expectedRentalOn);
    }

    @Transactional
    public long addOtherReservations(String stock_id) {
        return this.rentalManageRepository.addOtherReservations(stock_id);
    }

    @Transactional
    public long addOtherDates(String stock_id, Date expectedReturnOn, Date expectedRentalOn) {
        return this.rentalManageRepository.addOtherDates(stock_id, expectedReturnOn,expectedRentalOn);
    }
    
    @Transactional 
    public void save(RentalManageDto rentalManageDto) throws Exception {
        try {
            Account account = this.accountRepository.findByEmployeeId(rentalManageDto.getEmployeeId()).orElse(null);
            if (account == null) {
                throw new Exception("Account not found.");
            }

            Stock stock = this.stockRepository.findById(rentalManageDto.getStockId()).orElse(null);
            if (stock == null) {
                throw new Exception("Stock not found.");
            }

            RentalManage rentalManage = new RentalManage();
            rentalManage = setRentalStatusDate(rentalManage, rentalManageDto.getStatus());

            rentalManage.setAccount(account);
            rentalManage.setExpectedRentalOn(rentalManageDto.getExpectedRentalOn());
            rentalManage.setExpectedReturnOn(rentalManageDto.getExpectedReturnOn());
            rentalManage.setStatus(rentalManageDto.getStatus());
            rentalManage.setStock(stock);

            // データベースへの保存
            this.rentalManageRepository.save(rentalManage);
        } catch (Exception e) {
            throw e;
        }
    }

    private RentalManage setRentalStatusDate(RentalManage rentalManage, Integer status) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        
        if (status == RentalStatus.RENTAlING.getValue()) {
            rentalManage.setRentaledAt(timestamp);
        } else if (status == RentalStatus.RETURNED.getValue()) {
            rentalManage.setReturnedAt(timestamp);
        } else if (status == RentalStatus.CANCELED.getValue()) {
            rentalManage.setCanceledAt(timestamp);
        }

        return rentalManage;
    }
    @Transactional 
    public void update(Long id,RentalManageDto rentalManageDto) throws Exception {
        try {
            // 既存レコード取得
            Account account = this.accountRepository.findByEmployeeId(rentalManageDto.getEmployeeId()).orElse(null);
            Stock stock = this.stockRepository.findById(rentalManageDto.getStockId()).orElse(null);
            RentalManage updateTRental = findById(id);
            if (updateTRental == null) {
                throw new Exception("RentalManage record not found.");
            }

            updateTRental.setAccount(account);
            updateTRental.setExpectedRentalOn(rentalManageDto.getExpectedRentalOn());
            updateTRental.setExpectedReturnOn(rentalManageDto.getExpectedReturnOn());
            updateTRental.setStock(stock);
            updateTRental.setStatus(rentalManageDto.getStatus());
            

            // データベースへの保存
            this.rentalManageRepository.save(updateTRental);
        } catch (Exception e) {
            throw e;
        }
    }
}
