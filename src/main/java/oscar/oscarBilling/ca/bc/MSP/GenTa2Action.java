//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */


package oscar.oscarBilling.ca.bc.MSP;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oscarehr.billing.CA.BC.dao.TeleplanC12Dao;
import org.oscarehr.billing.CA.BC.dao.TeleplanS00Dao;
import org.oscarehr.billing.CA.BC.dao.TeleplanS21Dao;
import org.oscarehr.billing.CA.BC.dao.TeleplanS22Dao;
import org.oscarehr.billing.CA.BC.dao.TeleplanS23Dao;
import org.oscarehr.billing.CA.BC.dao.TeleplanS25Dao;
import org.oscarehr.billing.CA.BC.model.TeleplanC12;
import org.oscarehr.billing.CA.BC.model.TeleplanS00;
import org.oscarehr.billing.CA.BC.model.TeleplanS21;
import org.oscarehr.billing.CA.BC.model.TeleplanS22;
import org.oscarehr.billing.CA.BC.model.TeleplanS23;
import org.oscarehr.billing.CA.BC.model.TeleplanS25;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SpringUtils;

import oscar.OscarProperties;

/**
 * @author jay
 */
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

public class GenTa2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();


    private TeleplanS21Dao s21Dao = SpringUtils.getBean(TeleplanS21Dao.class);
    private TeleplanS00Dao s00Dao = SpringUtils.getBean(TeleplanS00Dao.class);
    private TeleplanS23Dao s23Dao = SpringUtils.getBean(TeleplanS23Dao.class);
    private TeleplanS25Dao s25Dao = SpringUtils.getBean(TeleplanS25Dao.class);
    private TeleplanS22Dao s22Dao = SpringUtils.getBean(TeleplanS22Dao.class);
    private TeleplanC12Dao c12Dao = SpringUtils.getBean(TeleplanC12Dao.class);


    /**
     * Creates a new instance of GenTaAction
     */
    public GenTa2Action() {
    }


    public String execute()
            throws IOException, ServletException, Exception {


        MSPReconcile mspReconcile = new MSPReconcile();


        int recFlag = 0;
        String raNo = "";
        String filename = (String) request.getAttribute("filename");// documentBean.getFilename();

        String forwardPage = "S21";

        String filepath = OscarProperties.getInstance().getProperty("DOCUMENT_DIR");

        FileInputStream file = new FileInputStream(filepath + filename);
        BufferedReader input = new BufferedReader(new InputStreamReader(file));
        String nextline;

        while ((nextline = input.readLine()) != null) {
            String header = nextline.substring(0, 3);
            if (header.equals("S21")) {
                S21 s21 = new S21();
                s21.parse(nextline);
                raNo = "";

                List<TeleplanS21> rs = s21Dao.findByFilenamePaymentPayeeNo(filename, s21.getT_payment(), s21.getT_payeeno());
                for (TeleplanS21 r : rs) {
                    raNo = String.valueOf(r.getId());
                }
                if (raNo.equals("") || raNo == null) {
                    recFlag = 1;
                    //persist here
                    TeleplanS21 t = new TeleplanS21();
                    t.setFileName(filename);
                    t.setDataCentre(s21.getT_datacenter());
                    t.setDataSeq(s21.getT_dataseq());
                    t.setPayment(s21.getT_payment());
                    t.setLineCode(s21.getT_linecode().toCharArray()[0]);
                    t.setPayeeNo(s21.getT_payeeno());
                    t.setMspCtlNo(s21.getT_mspctlno());
                    t.setPayeeName(s21.getT_payeename());
                    t.setAmountBilled(s21.getT_amtbilled());
                    t.setAmountPaid(s21.getT_amtpaid());
                    t.setBalanceForward(s21.getT_balancefwd());
                    t.setCheque(s21.getT_cheque());
                    t.setNewBalance(s21.getT_newbalance());
                    t.setFiller(s21.getT_filler());
                    t.setStatus('N');
                    s21Dao.persist(t);
                    raNo = t.getId().toString();
                }
            } else if (header.equals("S01")) {
                S01 s01 = new S01(nextline);
                if (recFlag > 0) {
                    TeleplanS00 t = new TeleplanS00();
                    t.setS21Id(Integer.parseInt(raNo));
                    t.setFileName(filename);
                    t.setS00Type(s01.t_s00type);
                    t.setDataCentre(s01.t_datacenter);
                    t.setDataSeq(s01.t_dataseq);
                    t.setPayment(s01.t_payment);
                    t.setLineCode(s01.t_linecode.toCharArray()[0]);
                    t.setPayeeNo(s01.t_payeeno);
                    t.setMspCtlNo(s01.t_mspctlno);
                    t.setPractitionerNo(s01.t_practitionerno);
                    t.setMspRcdDate(s01.t_msprcddate);
                    t.setInitial("");
                    t.setSurname("");
                    t.setPhn("");
                    t.setPhnDepNo("");
                    t.setServiceDate("");
                    t.setToday("");
                    t.setBillNoServices("");
                    t.setBillClafCode("");
                    t.setBillFeeSchedule("");
                    t.setBillAmount("");
                    t.setPaidNoServices("");
                    t.setPaidClafCode("");
                    t.setPaidFeeSchedule("");
                    t.setPaidAmount(s01.t_paidamt);
                    t.setOfficeNo(s01.t_officeno);
                    t.setExp1("");
                    t.setExp2("");
                    t.setExp3("");
                    t.setExp4("");
                    t.setExp5("");
                    t.setExp6("");
                    t.setExp7("");
                    t.setAjc1(s01.t_ajc1);
                    t.setAja1(s01.t_aja1);
                    t.setAjc2(s01.t_ajc2);
                    t.setAja2(s01.t_aja2);
                    t.setAjc3(s01.t_ajc3);
                    t.setAja3(s01.t_aja3);
                    t.setAjc4(s01.t_ajc4);
                    t.setAja4(s01.t_aja4);
                    t.setAjc5(s01.t_ajc5);
                    t.setAja5(s01.t_aja5);
                    t.setAjc6(s01.t_ajc6);
                    t.setAja6(s01.t_aja6);
                    t.setAjc7(s01.t_ajc7);
                    t.setAja7(s01.t_aja7);
                    t.setPaidRate(s01.t_paidrate);
                    t.setPlanRefNo("");
                    t.setClaimSource("");
                    t.setPreviousPaidDate("");
                    t.setIcBcWcb(s01.t_icbcwcb);
                    t.setInsurerCode(s01.t_insurercode);
                    t.setFiller(s01.t_filler);

                    s00Dao.persist(t);

                    mspReconcile.updateStat(MSPReconcile.SETTLED, s01.getBillingMasterNo());
                }
            } else if (header.equals("S02") || header.equals("S00") || header.equals("S03")) {
                S02 s02 = new S02(nextline);
                if (recFlag > 0) {
                    recFlag = recFlag + 1;

                    TeleplanS00 t = new TeleplanS00();
                    t.setS21Id(Integer.parseInt(raNo));
                    t.setFileName(filename);
                    t.setS00Type(s02.t_s00type);
                    t.setDataCentre(s02.t_datacenter);
                    t.setDataSeq(s02.t_dataseq);
                    t.setPayment(s02.t_payment);
                    t.setLineCode(s02.t_linecode.toCharArray()[0]);
                    t.setPayeeNo(s02.t_payeeno);
                    t.setMspCtlNo(s02.t_mspctlno);
                    t.setPractitionerNo(s02.t_practitionerno);
                    t.setMspRcdDate(s02.t_msprcddate);
                    t.setInitial(s02.t_initial);
                    t.setSurname(s02.t_surname);
                    t.setPhn(s02.t_phn);
                    t.setPhnDepNo(s02.t_phndepno);
                    t.setServiceDate(s02.t_servicedate);
                    t.setToday(s02.t_today);
                    t.setBillNoServices(s02.t_billnoservices);
                    t.setBillClafCode(s02.t_billclafcode);
                    t.setBillFeeSchedule(s02.t_billfeeschedule);
                    t.setBillAmount(s02.t_billamt);
                    t.setPaidNoServices(s02.t_paidnoservices);
                    t.setPaidClafCode(s02.t_paidclafcode);
                    t.setPaidFeeSchedule(s02.t_paidfeeschedule);
                    t.setPaidAmount(s02.t_paidamt);
                    t.setOfficeNo(s02.t_officeno);
                    t.setExp1(s02.t_exp1);
                    t.setExp2(s02.t_exp2);
                    t.setExp3(s02.t_exp3);
                    t.setExp4(s02.t_exp4);
                    t.setExp5(s02.t_exp5);
                    t.setExp6(s02.t_exp6);
                    t.setExp7(s02.t_exp7);
                    t.setAjc1(s02.t_ajc1);
                    t.setAja1(s02.t_aja1);
                    t.setAjc2(s02.t_ajc2);
                    t.setAja2(s02.t_aja2);
                    t.setAjc3(s02.t_ajc3);
                    t.setAja3(s02.t_aja3);
                    t.setAjc4(s02.t_ajc4);
                    t.setAja4(s02.t_aja4);
                    t.setAjc5(s02.t_ajc5);
                    t.setAja5(s02.t_aja5);
                    t.setAjc6(s02.t_ajc6);
                    t.setAja6(s02.t_aja6);
                    t.setAjc7(s02.t_ajc7);
                    t.setAja7(s02.t_aja7);
                    t.setPaidRate("");
                    t.setPlanRefNo(s02.t_planrefno);
                    t.setClaimSource(s02.t_claimsource);
                    t.setPreviousPaidDate(s02.t_previouspaiddate);
                    t.setIcBcWcb(s02.t_icbcwcb);
                    t.setInsurerCode(s02.t_insurercode);
                    t.setFiller(s02.t_filler);

                    s00Dao.persist(t);


                    if (header.equals("S02")) { //header.compareTo("S00") == 0 || header.compareTo("S03") == 0){
                        mspReconcile.updateStat(MSPReconcile.PAIDWITHEXP, s02.getBillingMasterNo());
                    } else if (header.equals("S03")) {
                        mspReconcile.updateStat(MSPReconcile.REFUSED, s02.getBillingMasterNo());
                    } else if (header.equals("S00")) {
                        mspReconcile.updateStat(MSPReconcile.DATACENTERCHANGED, s02.getBillingMasterNo());
                    }
                }
            } else if (header.equals("S04")) {
                S04 s04 = new S04(nextline);
                if (recFlag > 0) {
                    TeleplanS00 t = new TeleplanS00();
                    t.setS21Id(Integer.parseInt(raNo));
                    t.setFileName(filename);
                    t.setS00Type(s04.t_s00type);
                    t.setDataCentre(s04.t_datacenter);
                    t.setDataSeq(s04.t_dataseq);
                    t.setPayment(s04.t_payment);
                    t.setLineCode(s04.t_linecode.toCharArray()[0]);
                    t.setPayeeNo(s04.t_payeeno);
                    t.setMspCtlNo(s04.t_mspctlno);

                    t.setPractitionerNo(s04.t_practitionerno);
                    t.setMspRcdDate("");
                    t.setInitial("");
                    t.setSurname("");
                    t.setPhn("");
                    t.setPhnDepNo("");
                    t.setServiceDate("");
                    t.setToday("");
                    t.setBillNoServices("");
                    t.setBillClafCode("");
                    t.setBillFeeSchedule("");
                    t.setBillAmount("");
                    t.setPaidNoServices("");
                    t.setPaidClafCode("");
                    t.setPaidFeeSchedule("");
                    t.setPaidAmount("");
                    t.setOfficeNo(s04.t_officeno);
                    t.setExp1(s04.t_exp1);
                    t.setExp2(s04.t_exp2);
                    t.setExp3(s04.t_exp3);
                    t.setExp4(s04.t_exp4);
                    t.setExp5(s04.t_exp5);
                    t.setExp6(s04.t_exp6);
                    t.setExp7(s04.t_exp7);
                    t.setAjc1("");
                    t.setAja1("");
                    t.setAjc2("");
                    t.setAja2("");
                    t.setAjc3("");
                    t.setAja3("");
                    t.setAjc4("");
                    t.setAja4("");
                    t.setAjc5("");
                    t.setAja5("");
                    t.setAjc6("");
                    t.setAja6("");
                    t.setAjc7("");
                    t.setAja7("");
                    t.setPaidRate("");
                    t.setPlanRefNo("");
                    t.setClaimSource("");
                    t.setPreviousPaidDate("");
                    t.setIcBcWcb(s04.t_icbcwcb);
                    t.setInsurerCode(s04.t_insurercode);
                    t.setFiller(s04.t_filler);

                    s00Dao.persist(t);


                    mspReconcile.updateStat(MSPReconcile.HELD, s04.getBillingMasterNo());
                }
            } else if (header.equals("S23") || header.equals("S24")) {
                S23 s23 = new S23(nextline);
                if (recFlag > 0) {
                    TeleplanS23 t = new TeleplanS23();
                    t.setS21Id(Integer.parseInt(raNo));
                    t.setFileName(filename);
                    t.setS23Type(s23.t_s23type);
                    t.setDataCentre(s23.t_datacenter);
                    t.setDataSeq(s23.t_dataseq);
                    t.setPayment(s23.t_payment);
                    t.setLineCode(s23.t_linecode.charAt(0));
                    t.setMspCtlNo(s23.t_mspctlno);
                    t.setAjc(s23.t_ajc);
                    t.setAji(s23.t_aji);
                    t.setAjm(s23.t_ajm);
                    t.setCalcMethod(s23.t_calcmethod);
                    t.setrPercent(s23.t_rpercent);
                    t.setoPercent(s23.t_opercent);
                    t.setgAmount(s23.t_gamount);
                    t.setrAmount(s23.t_ramount);
                    t.setoAmount(s23.t_oamount);
                    t.setBalanceForward(s23.t_balancefwd);
                    t.setAdjMade(s23.t_adjmade);
                    t.setAdjOutstanding(s23.t_adjoutstanding);
                    t.setFiller(s23.t_filler);

                    s23Dao.persist(t);

                }
            } else if (header.equals("S25")) {
                S25 s25 = new S25(nextline);
                if (recFlag > 0) {
                    TeleplanS25 t = new TeleplanS25();
                    t.setS21Id(Integer.parseInt(raNo));
                    t.setFileName(filename);
                    t.setS25Type(s25.t_s25type);
                    t.setDataCentre(s25.t_datacenter);
                    t.setDataSeq(s25.t_dataseq);
                    t.setPayment(s25.t_payment);
                    t.setLineCode(s25.t_linecode.charAt(0));
                    t.setPayeeNo(s25.t_payeeno);
                    t.setMspCtlNo(s25.t_mspctlno);
                    t.setPractitionerNo(s25.t_practitionerno);
                    t.setMessage(s25.t_message);
                    t.setFiller(s25.t_filler);

                    s25Dao.persist(t);
                }
            } else if (header.equals("S22")) {
                S22 s22 = new S22(nextline);
                if (recFlag > 0) {
                    TeleplanS22 t = new TeleplanS22();
                    t.setS21Id(Integer.parseInt(raNo));
                    t.setFileName(filename);
                    t.setS22Type(s22.t_s22type);
                    t.setDataCentre(s22.t_datacenter);
                    t.setDataSeq(s22.t_dataseq);
                    t.setPayment(s22.t_payment);
                    t.setLineCode(s22.t_linecode.charAt(0));
                    t.setPayeeNo(s22.t_payeeno);
                    t.setMspCtlNo(s22.t_mspctlno);
                    t.setPractitionerNo(s22.t_practitionerno);
                    t.setPractitionerName(s22.t_practitionername);
                    t.setAmountBilled(s22.t_amtbilled);
                    t.setAmountPaid(s22.t_amtpaid);
                    t.setFiller(s22.t_filler);
                    s22Dao.persist(t);
                }

                /*
                 *C12 records are error records. There are three ways that the program will come to here
                 *1.File with just C12 records (besides all the VCR ones at the top)
                 *     one record is added to teleplanS21
                 *2.File with C12 records at the top before it gets to a S21 record
                 *     two records are added to teleplanS21, one with a status of D ( the one from the C12 records ) and the other with N
                 *3.File with C12 records at the bottom.
                 *     one record with a status of N
                 *
                 */
            } else if (header.equals("C12")) {
                C12 c12 = new C12(nextline);
                if (raNo.equals("")) {

                    List<TeleplanS21> rs = s21Dao.findByFilenamePaymentPayeeNo(filename, "", "");
                    for (TeleplanS21 r : rs) {
                        raNo = String.valueOf(r.getId());
                    }

                    if (raNo.compareTo("") == 0 || raNo == null) {
                        recFlag = 1;

                        TeleplanS21 t = new TeleplanS21();
                        t.setFileName(filename);
                        t.setDataCentre(c12.getT_datacenter());
                        t.setDataSeq(c12.getT_dataseq());
                        t.setPayment("");
                        t.setPayeeNo(c12.getT_payeeno());
                        t.setMspCtlNo("");
                        t.setPayeeName("");
                        t.setAmountBilled("");
                        t.setAmountPaid("");
                        t.setBalanceForward("");
                        t.setCheque("");
                        t.setNewBalance("");
                        t.setFiller("");
                        t.setStatus('D');
                        s21Dao.persist(t);
                        raNo = t.getId().toString();
                    }
                }  // This will be +1 if the records are at the bottom
                if (recFlag > 0) {
                    TeleplanC12 t = new TeleplanC12();
                    t.setS21Id(Integer.parseInt(raNo));
                    t.setFileName(filename);
                    t.setDataCentre(c12.getT_datacenter());
                    t.setDataSeq(c12.getT_dataseq());
                    t.setPayeeNo(c12.getT_payeeno());
                    t.setPractitionerNo(c12.getT_practitionerno());
                    t.setExp1(c12.getT_exp1());
                    t.setExp2(c12.getT_exp2());
                    t.setExp3(c12.getT_exp3());
                    t.setExp4(c12.getT_exp4());
                    t.setExp5(c12.getT_exp5());
                    t.setExp6(c12.getT_exp6());
                    t.setExp7(c12.getT_exp7());
                    t.setOfficeFolioClaimNo(c12.getT_officefolioclaimno());
                    t.setFiller(c12.getT_filler());

                    c12Dao.persist(t);

                    mspReconcile.updateStat(MSPReconcile.REJECTED, c12.getBillingMasterNo());
                }
                forwardPage = "C12";

            } else if (header.equals("M01")) {

            }
        }
        return forwardPage;
    }
}
