//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright (c) 2009-2018 uniCenta
//    https://unicenta.com
//
//    This file is part of uniCenta oPOS
//
//    uniCenta oPOS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//   uniCenta oPOS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>.
package com.unicenta.pos.ticket;

import com.unicenta.basic.BasicException;
import com.unicenta.data.loader.DataRead;
import com.unicenta.data.loader.LocalRes;
import com.unicenta.data.loader.SerializableRead;
import com.unicenta.format.Formats;
import com.unicenta.pos.customers.CustomerInfoExt;
import com.unicenta.pos.forms.AppConfig;
import com.unicenta.pos.forms.AppLocal;
import com.unicenta.pos.payment.PaymentInfo;
import com.unicenta.pos.payment.PaymentInfoMagcard;
import com.unicenta.pos.payment.PaymentInfoTicket;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author adrianromero
 */
public final class TicketInfo implements SerializableRead, Externalizable {

  private static final long serialVersionUID = 2765650092387265178L;

  public static final int RECEIPT_NORMAL = 0;
  public static final int RECEIPT_REFUND = 1;
  public static final int RECEIPT_PAYMENT = 2;
  public static final int RECEIPT_NOSALE = 3;

  // JG Jun 2017 - contain partial/full refunds
  public static final int REFUND_NOT = 0; // is a non-refunded ticket
  public static final int REFUND_PARTIAL = 1;
  public static final int REFUND_ALL = 2;

  private static final DateFormat m_dateformat = new SimpleDateFormat("hh:mm");

  private String m_sHost;
  private String m_sId;
  private int tickettype;
  private int m_iTicketId;
  private int m_iPickupId;
  private java.util.Date m_dDate;
  private Properties attributes;
  private UserInfo m_User;
  private Double multiply;
  private CustomerInfoExt m_Customer;
  private String m_sActiveCash;
  private List<TicketLineInfo> m_aLines;
  private List<PaymentInfo> payments;
  private List<TicketTaxInfo> taxes;
  private final String m_sResponse;
  private String loyaltyCardNumber;
  private Boolean oldTicket;
  private boolean tip;
  private PaymentInfoTicket m_paymentInfo;
  private boolean m_isProcessed;
  private final String m_locked;
  private Double nsum;
  private int ticketstatus;

  private static String hostname;

  // New properties for serie
  private String code;
  private String serie;

  private String serieNumber;
  private String formatNumberDigits;

  private String ticketTicketId;
  private String environment; // Test -> 1; Production -> 2
  private String accessKey;

  private String comercialName;
  private String addressEstablishment;
  private String phoneEstablishment;
  private String emailEstablishment;

  public String getAccessKey() {
    return accessKey;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getSerie() {
    return serie;
  }

  public void setSerie(String serie) {
    this.serie = serie;
  }

  public String getSerieNumber() {
    return serieNumber;
  }

  public void setSerieNumber(String serieNumber) {
    this.serieNumber = serieNumber;
  }

  public String getFormatNumberDigits() {
    return formatNumberDigits;
  }

  public void setFormatNumberDigits(String formatNumberDigits) {
    this.formatNumberDigits = formatNumberDigits;
  }

  public String getTicketTicketId() {
    return ticketTicketId;
  }

  public void setTicketTicketId(String ticketTicketId) {
    this.ticketTicketId = ticketTicketId;
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public String getComercialName() {
    return comercialName;
  }

  public void setComercialName(String comercialName) {
    this.comercialName = comercialName;
  }

  public String getAddressEstablishment() {
    return addressEstablishment;
  }

  public void setAddressEstablishment(String addressEstablishment) {
    this.addressEstablishment = addressEstablishment;
  }

  public String getPhoneEstablishment() {
    return phoneEstablishment;
  }

  public void setPhoneEstablishment(String phoneEstablishment) {
    this.phoneEstablishment = phoneEstablishment;
  }

  public String getEmailEstablishment() {
    return emailEstablishment;
  }

  public void setEmailEstablishment(String emailEstablishment) {
    this.emailEstablishment = emailEstablishment;
  }

  public static void setHostname(String name) {
    hostname = name;
  }

  public static String getHostname() {
    return hostname;
  }

  /** Creates new TicketModel */
  public TicketInfo() {
    m_sId = UUID.randomUUID().toString();
    tickettype = RECEIPT_NORMAL;
    m_iTicketId = 0; // incrementamos
    m_dDate = new Date();
    attributes = new Properties();
    m_User = null;
    m_Customer = null;
    m_sActiveCash = null;
    m_aLines = new ArrayList<>();
    payments = new ArrayList<>();
    taxes = null;
    m_sResponse = null;
    oldTicket = false;

    AppConfig config =
        new AppConfig(
            new File(new File(System.getProperty("user.home")), AppLocal.APP_ID + ".properties"));
    config.load();
    tip = Boolean.valueOf(config.getProperty("machine.showTip"));
    m_isProcessed = false;
    m_locked = null;
    ticketstatus = 0;
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(m_sId);
    out.writeInt(tickettype);
    out.writeInt(m_iTicketId);
    out.writeObject(m_Customer);
    out.writeObject(m_dDate);
    out.writeObject(attributes);
    out.writeObject(m_aLines);

    out.writeInt(ticketstatus);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    m_sId = (String) in.readObject();
    tickettype = in.readInt();
    m_iTicketId = in.readInt();
    m_Customer = (CustomerInfoExt) in.readObject();
    m_dDate = (Date) in.readObject();
    attributes = (Properties) in.readObject();
    m_aLines = (List<TicketLineInfo>) in.readObject();
    m_User = null;
    m_sActiveCash = null;
    payments = new ArrayList<>(); // JG June 2102 diamond inference
    taxes = null;

    ticketstatus = in.readInt();
  }

  /**
   * @param dr
   * @throws BasicException
   */
  @Override
  public void readValues(DataRead dr) throws BasicException {
    m_sId = dr.getString(1);
    tickettype = dr.getInt(2);
    m_iTicketId = dr.getInt(3);
    m_dDate = dr.getTimestamp(4);
    m_sActiveCash = dr.getString(5);
    try {
      byte[] img = dr.getBytes(6);
      if (img != null) {
        attributes.loadFromXML(new ByteArrayInputStream(img));
      }
    } catch (IOException e) {
    }
    m_User = new UserInfo(dr.getString(7), dr.getString(8));
    m_Customer = new CustomerInfoExt(dr.getString(9));
    m_aLines = new ArrayList<>();
    payments = new ArrayList<>();
    taxes = null;

    ticketstatus = dr.getInt(10);
    code = dr.getString(11);
    serieNumber = dr.getString(12);
  }

  /**
   * @return
   */
  public TicketInfo copyTicket() {
    TicketInfo t = new TicketInfo();

    t.tickettype = tickettype;
    t.m_iTicketId = m_iTicketId;
    t.m_dDate = m_dDate;
    t.m_sActiveCash = m_sActiveCash;
    t.attributes = (Properties) attributes.clone();
    t.m_User = m_User;
    t.m_Customer = m_Customer;

    t.m_aLines = new ArrayList<>(); // JG June 2102 diamond inference
    m_aLines.forEach(
        (l) -> {
          t.m_aLines.add(l.copyTicketLine());
        });
    t.refreshLines();

    t.payments = new LinkedList<>(); // JG June 2102 diamond inference
    payments.forEach(
        (p) -> {
          t.payments.add(p.copyPayment());
        });
    t.oldTicket = oldTicket;
    // taxes are not copied, must be calculated again.

    t.ticketstatus = ticketstatus;

    return t;
  }

  public String getId() {
    return m_sId;
  }

  public int getTicketType() {
    return tickettype;
  }

  public void setTicketType(int tickettype) {
    this.tickettype = tickettype;
  }

  public int getTicketId() {
    return m_iTicketId;
  }

  public void setTicketId(int iTicketId) {
    m_iTicketId = iTicketId;
  }

  public int getTicketStatus() {
    return ticketstatus;
  }

  public void setTicketStatus(int ticketstatus) {
    if (m_iTicketId > 0) {
      this.ticketstatus = m_iTicketId;
    } else {
      this.ticketstatus = ticketstatus;
    }
  }

  public void setPickupId(int iTicketId) {
    m_iPickupId = iTicketId;
  }

  public int getPickupId() {
    return m_iPickupId;
  }

  public java.util.Date getDate() {
    return m_dDate;
  }

  public void setDate(java.util.Date dDate) {
    m_dDate = dDate;
  }

  public String getHost() {
    AppConfig m_config_host =
        new AppConfig(new File((System.getProperty("user.home")), AppLocal.APP_ID + ".properties"));
    m_config_host.load();
    String machineHostname = (m_config_host.getProperty("machine.hostname"));
    m_config_host = null;
    return machineHostname;
  }

  public UserInfo getUser() {
    return m_User;
  }

  public void setUser(UserInfo value) {
    m_User = value;
  }

  public CustomerInfoExt getCustomer() {
    return m_Customer;
  }

  public void setCustomer(CustomerInfoExt value) {
    m_Customer = value;
  }

  public String getCustomerId() {
    if (m_Customer == null) {
      return null;
    } else {
      return m_Customer.getId();
    }
  }

  public String getReturnMessage() {
    return ((getPayments().get(getPayments().size() - 1)) instanceof PaymentInfoMagcard)
        ? ((PaymentInfoMagcard) (getPayments().get(getPayments().size() - 1))).getReturnMessage()
        : LocalRes.getIntString("button.OK");
  }

  public void setActiveCash(String value) {
    m_sActiveCash = value;
  }

  public String getActiveCash() {
    return m_sActiveCash;
  }

  public String getProperty(String key) {
    return attributes.getProperty(key);
  }

  public String getProperty(String key, String defaultvalue) {
    return attributes.getProperty(key, defaultvalue);
  }

  public void setProperty(String key, String value) {
    attributes.setProperty(key, value);
  }

  public Properties getProperties() {
    return attributes;
  }

  public TicketLineInfo getLine(int index) {
    return m_aLines.get(index);
  }

  public void addLine(TicketLineInfo oLine) {
    oLine.setTicket(m_sId, m_aLines.size());
    m_aLines.add(oLine);
  }

  public void insertLine(int index, TicketLineInfo oLine) {
    m_aLines.add(index, oLine);
    refreshLines();
  }

  public void setLine(int index, TicketLineInfo oLine) {
    oLine.setTicket(m_sId, index);
    m_aLines.set(index, oLine);
  }

  public void removeLine(int index) {
    m_aLines.remove(index);
    refreshLines();
  }

  public void refreshLines() {
    for (int i = 0; i < m_aLines.size(); i++) {
      getLine(i).setTicket(m_sId, i);
    }
  }

  public int getLinesCount() {
    return m_aLines.size();
  }

  public double getArticlesCount() {
    double dArticles = 0.0;
    TicketLineInfo oLine;

    for (Iterator<TicketLineInfo> i = m_aLines.iterator(); i.hasNext(); ) {
      oLine = i.next();
      dArticles += oLine.getMultiply();
    }

    return dArticles;
  }

  public double getSubTotal() {
    double sum = 0.0;
    sum =
        m_aLines.stream()
            .map((line) -> line.getSubValue())
            .reduce(sum, (accumulator, _item) -> accumulator + _item);
    return sum;
  }

  public double getTax() {

    double sum = 0.0;
    if (hasTaxesCalculated()) {
      for (TicketTaxInfo tax : taxes) {
        sum += tax.getTax(); // Taxes are already rounded...
        nsum = sum;
      }
    } else {
      sum =
          m_aLines.stream()
              .map((line) -> line.getTax())
              .reduce(sum, (accumulator, _item) -> accumulator + _item);
    }
    return sum;
  }

  public double getSubTotalIVA() {

    double sum = 0.0;

    for (TicketLineInfo line : m_aLines) {
      if (line.getTaxInfo().getRate() > 0) {
        sum += line.getSubValue();
      }
    }

    return sum;
  }

  public double getSubTotalIVA0() {

    double sum = 0.0;

    for (TicketLineInfo line : m_aLines) {
      if (line.getTaxInfo().getRate() == 0) {
        sum += line.getSubValue();
      }
    }

    return sum;
  }

  public double getTotal() {
    return getSubTotal() + getTax();
  }

  public double getServiceCharge() {
    return (getTotal() + getTax());
  }

  public double getTotalPaid() {
    double sum = 0.0;
    sum =
        payments.stream()
            .filter((p) -> (!"debtpaid".equals(p.getName())))
            .map((p) -> p.getTotal())
            .reduce(sum, (accumulator, _item) -> accumulator + _item);
    return sum;
  }

  public double getTendered() {
    return getTotalPaid();
  }

  public List<TicketLineInfo> getLines() {
    return m_aLines;
  }

  public void setLines(List<TicketLineInfo> l) {
    m_aLines = l;
  }

  public List<PaymentInfo> getPayments() {
    return payments;
  }

  public void setPayments(List<PaymentInfo> l) {
    payments = l;
  }

  public void resetPayments() {
    payments = new ArrayList<>(); // JG June 2102 diamond inference
  }

  public List<TicketTaxInfo> getTaxes() {
    return taxes;
  }

  public boolean hasTaxesCalculated() {
    return taxes != null;
  }

  public void setTaxes(List<TicketTaxInfo> l) {
    taxes = l;
  }

  public void resetTaxes() {
    taxes = null;
  }

  public void setTip(boolean tips) {
    tip = tips;
  }

  public boolean hasTip() {
    return tip;
  }

  public void setIsProcessed(boolean isP) {
    m_isProcessed = isP;
  }

  public TicketTaxInfo getTaxLine(TaxInfo tax) {

    for (TicketTaxInfo taxline : taxes) {
      if (tax.getId().equals(taxline.getTaxInfo().getId())) {
        return taxline;
      }
    }

    return new TicketTaxInfo(tax);
  }

  public TicketTaxInfo[] getTaxLines() {

    Map<String, TicketTaxInfo> m = new HashMap<>();

    TicketLineInfo oLine;
    for (Iterator<TicketLineInfo> i = m_aLines.iterator(); i.hasNext(); ) {
      oLine = i.next();

      TicketTaxInfo t = m.get(oLine.getTaxInfo().getId());
      if (t == null) {
        t = new TicketTaxInfo(oLine.getTaxInfo());
        m.put(t.getTaxInfo().getId(), t);
      }
      t.add(oLine.getSubValue());
    }

    // return dSuma;
    Collection<TicketTaxInfo> avalues = m.values();
    return avalues.toArray(new TicketTaxInfo[avalues.size()]);
  }

  public String printId() {

    AppConfig m_config =
        new AppConfig(new File((System.getProperty("user.home")), AppLocal.APP_ID + ".properties"));
    m_config.load();
    String receiptSize = (m_config.getProperty("till.receiptsize"));
    String receiptPrefix = (m_config.getProperty("till.receiptprefix"));

    m_config = null;

    if (m_iTicketId > 0) {
      String tmpTicketId = Integer.toString(m_iTicketId);
      if (receiptSize == null || (Integer.parseInt(receiptSize) <= tmpTicketId.length())) {
        if (receiptPrefix != null) {
          tmpTicketId = receiptPrefix + tmpTicketId;
        }
        return tmpTicketId;
      }
      while (tmpTicketId.length() < Integer.parseInt(receiptSize)) {
        tmpTicketId = "0" + tmpTicketId;
      }
      if (receiptPrefix != null) {
        tmpTicketId = receiptPrefix + tmpTicketId;
      }
      return tmpTicketId;
    } else {
      return "";
    }
  }

  public String printDate() {
    return Formats.TIMESTAMP.formatValue(m_dDate);
  }

  public String printUser() {
    return m_User == null ? "" : m_User.getName();
  }

  public String printHost() {
    return m_sHost;
  }

  // Added JDL 28.05.13 for loyalty card functions

  /** */
  public void clearCardNumber() {
    loyaltyCardNumber = null;
  }

  /**
   * @param cardNumber
   */
  public void setLoyaltyCardNumber(String cardNumber) {
    loyaltyCardNumber = cardNumber;
  }

  /**
   * @return
   */
  public String getLoyaltyCardNumber() {
    return (loyaltyCardNumber);
  }

  public String printPhone1() {
    return m_Customer == null ? "" : m_Customer.getPhone1();
  }

  public String printArticlesCount() {
    return Formats.DOUBLE.formatValue(getArticlesCount());
  }

  public String printSubTotal() {
    return Formats.CURRENCY.formatValue(getSubTotal());
  }

  public String printTax() {
    return Formats.CURRENCY.formatValue(getTax());
  }

  public String printSubTotalIVA() {
    return Formats.CURRENCY.formatValue(getSubTotalIVA());
  }

  public String printSubTotalIVA0() {
    return Formats.CURRENCY.formatValue(getSubTotalIVA0());
  }

  public String printTotal() {
    return Formats.CURRENCY.formatValue(getTotal());
  }

  public String printTotalPaid() {
    return Formats.CURRENCY.formatValue(getTotalPaid());
  }

  public String printTendered() {
    return Formats.CURRENCY.formatValue(getTendered());
  }

  public String VoucherReturned() {
    return Formats.CURRENCY.formatValue(getTotalPaid() - getTotal());
  }

  public boolean getOldTicket() {
    return (oldTicket);
  }

  public void setOldTicket(Boolean otState) {
    oldTicket = otState;
  }

  public String getTicketHeaderFooterData(String data) {
    AppConfig m_config =
        new AppConfig(new File((System.getProperty("user.home")), AppLocal.APP_ID + ".properties"));
    m_config.load();
    String row = (m_config.getProperty("tkt." + data));

    return row;
  }

  public String printQRCode(String nameHeader, String vatHeader, String vatLabel) {
    String name = getTicketHeaderFooterData(nameHeader);
    String vatNumberHeader = getTicketHeaderFooterData(vatHeader);
    String vatNumber = vatNumberHeader.split(vatLabel)[1];
    String date = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss").format(getDate());
    String total = String.valueOf(getTotal());
    String vatValue = String.valueOf(getTax());
    return name + "," + vatNumber + "," + date + "," + total + "," + vatValue;
  }

  public String printTicketHeaderLine1() {
    String lineData = getTicketHeaderFooterData("header1");

    if (lineData != null) {
      return lineData;
    } else {
      return "";
    }
  }

  public String printTicketHeaderLine2() {
    String lineData = getTicketHeaderFooterData("header2");

    if (lineData != null) {
      return lineData;
    } else {
      return "";
    }
  }

  public String printTicketHeaderLine3() {
    String lineData = getTicketHeaderFooterData("header3");

    if (lineData != null) {
      return lineData;
    } else {
      return "";
    }
  }

  public String printTicketHeaderLine4() {
    String lineData = getTicketHeaderFooterData("header4");

    if (lineData != null) {
      return lineData;
    } else {
      return "";
    }
  }

  public String printTicketHeaderLine5() {
    String lineData = getTicketHeaderFooterData("header5");

    if (lineData != null) {
      return lineData;
    } else {
      return "";
    }
  }

  public String printTicketHeaderLine6() {
    String lineData = getTicketHeaderFooterData("header6");

    if (lineData != null) {
      return lineData;
    } else {
      return "";
    }
  }

  public String printTicketFooterLine1() {
    String lineData = getTicketHeaderFooterData("footer1");

    if (lineData != null) {
      return lineData;
    } else {
      return "";
    }
  }

  public String printTicketFooterLine2() {
    String lineData = getTicketHeaderFooterData("footer2");

    if (lineData != null) {
      return lineData;
    } else {
      return "";
    }
  }

  public String printTicketFooterLine3() {
    String lineData = getTicketHeaderFooterData("footer3");

    if (lineData != null) {
      return lineData;
    } else {
      return "";
    }
  }

  public String printTicketFooterLine4() {
    String lineData = getTicketHeaderFooterData("footer4");

    if (lineData != null) {
      return lineData;
    } else {
      return "";
    }
  }

  public String printTicketFooterLine5() {
    String lineData = getTicketHeaderFooterData("footer5");

    if (lineData != null) {
      return lineData;
    } else {
      return "";
    }
  }

  public String printTicketFooterLine6() {
    String lineData = getTicketHeaderFooterData("footer6");

    if (lineData != null) {
      return lineData;
    } else {
      return "";
    }
  }
}
