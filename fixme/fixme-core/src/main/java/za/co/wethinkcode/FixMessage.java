package za.co.wethinkcode;

// Documentation:
//
// What an actual Fix 4.0 sell message looks like ('|' == ASCII SOH, signals EOF):
//
// Header:
//
//    8=FIX.4.0|                      (Fix Version)
//    9=176|                          (Body Length)
//    35=D|                           (Message Type)
//    49=4gdkSL|                      (Source ID)
//    56=q5cXxF|                      (Target ID)
//    34=1|                           (Message Sequence Number)
//    52=20071123-05:30:00.000|       (Time Message Sent)
//
// Body:
//
//    11=Broker1Order1|               (ClOrdID - Broker / Institution Order ID)
//    21=1|                           (Order Handling, 1 == Auto Exec)
//    55=XAU|                         (Symbol, XAU == Gold)
//    54=2|                           (Side, 1 == Buy Side, 2 == Sell Side)
//    38=1250|                        (Order Quantity)
//    40=1|                           (Order Type, 1 == Market, 2 == Limit...)
//
// Footer:
//
//    10=128|                         (Checksum)
//
// The tags above are all required.


// The broker will send either buy or sell messages, the market will simply
// reply using an execution report.
//
// For simplicity, we wont be using SOH (0x1), we'll simply be using the pipe
// symbol itself.
//
// Tag 11, i.e. unique order id, if message type is and order (single), should
// ideally be unique and random as to protect against fakes, but meh. Tag 11
// is ClOrdID.
//
// Tag 9, i.e. message length, needs to be added last, which means everything
// else will need to be calculated / computed first. The header obviously needs
// to get sent first, followed by the body and the footer.
//
// Calculating the message length is not difficult as most tag values are of
// constant length.
//
// How the body length is calculated (we're using .length() just in case changes
// are made). This is for buy or sell messages:
//
// 3 + 1 (messageType) + 1 +
// 3 + 6 (sourceID) + 1 +
// 3 + 6 (targetID) + 1 +
// 3 + 1 (messageSeqNum) + 1 +
// 3 + 21 (messageTimeSent) + 1 +
//
// 3 + brokerOrderID.length() + 1 +
// 3 + 1 (orderHandling) + 1 +
// 3 + 3 (Symbol) + 1 +
// 3 + 1 (buyOrSell) + 1 +
// 3 + orderAmount.length() + 1 +
// 3 + 1 (orderType) + 1
//
// Body length calculation for an execution report (AvgPx == 2, not 3):
//
// 3 + 1 (messageType) + 1 +
// 3 + 6 (sourceID) + 1 +
// 3 + 6 (targetID) + 1 +
// 3 + 1 (messageSeqNum) + 1 +
// 3 + 21 (messageTimeSent) + 1 +
//
// 3 + brokerOrderID.length() + 1 +
// 3

// Supported FIX 4.0 Messages:
//
// 1) Single Buy Order.
// 2) Single Sell Order.
// 3) Execution Report, will indicate if an order is 'filled' (executed) or
//    rejected.

import za.co.wethinkcode.exceptions.FixMessageException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class FixMessage {

  // BS == Used by buy and sell messages.
  // ER == Used by execution reports.
  // BO == Used by both.

  // Set by constructor.
  private String messageType;             // BO
  private String sourceID;                // BO
  private String targetID;                // BO, swap target / source in market reply.
  private String brokerOrderID;           // BO
  private String symbol;                  // BO
  private String buyOrSell;               // BO
  private String orderAmount;             // BO
  private String marketOrderID;           // ER, use brokerOrderID in reply.
  private String orderStatus;             // ER, 2 == Filled, 8 == Rejected.
  private String filledAmount;            // ER
  private String avgFilledPrice;          // ER

  // These are either constants or calculated.
  private String fixVersion = "FIX.4.0";  // BO
  private String bodyLength;              // BO
  private String messageSeqNum = "1";     // BO
  private String messageTimeSent;         // BO
  private String orderHandling = "1";     // BS
  private String orderType = "1";         // BS
  private String execID = "0";            // ER
  private String execTransType = "3";     // ER

  // This is needed to get calculate the checksum.
  private String fixMessageWithoutChecksum;

  private String checkSum;

  // Final FIX.4.0 message.
  private String finalFixMessage;

  // Invokes on buy or sell messages.
  public FixMessage(
    String messageType,
    String sourceID,
    String targetID,
    String brokerOrderID,
    String symbol,
    String buyOrSell,
    String orderAmount
  ) throws FixMessageException {

    // TODO: Validate input here.

    this.messageType = messageType;
    this.sourceID = sourceID;
    this.targetID = targetID;
    this.brokerOrderID = brokerOrderID;
    this.symbol = symbol;
    this.buyOrSell = buyOrSell;
    this.orderAmount = orderAmount;
    this.messageTimeSent = this.calcGMTTime();
    this.bodyLength = this.calcBodyLength(messageType);
    this.fixMessageWithoutChecksum = this.getFixMessageWithoutChecksum(messageType);
    this.checkSum = CheckSum.generateCheckSum(this.fixMessageWithoutChecksum);

    // TODO: Set final fix message here.
  }

  // Invokes on execution report messages.
  public FixMessage(
    String messageType,
    String sourceID,
    String targetID,
    String marketOrderID,
    String orderStatus,
    String symbol,
    String buyOrSell,
    String orderAmount,
    String filledAmount,
    String avgFilledPrice
  ) {

    // TODO: Set checksum here.
    // TODO: Set final fix message here.
  }

  public void sendFixMessage() {

    // TODO: Fill this in, this should take in the socket.
  }

  private void validateFixInput(
    String messageType,
    String sourceID,
    String targetID,
    String brokerOrderID,
    String symbol,
    String buyOrSell,
    String orderAmount
  ) throws FixMessageException {

    // TODO: Implement this.
  }

  private String calcGMTTime() {

    Date currentTime = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    return sdf.format(currentTime);
  }

  private String calcBodyLength(String messageType) {

    if (messageType.equals("D")) {

      int bodyLength = (3 * 11) + 11 +
        this.messageType.length() +
        this.sourceID.length() +
        this.targetID.length() +
        this.messageSeqNum.length() +
        this.messageTimeSent.length() +
        this.brokerOrderID.length() +
        this.orderHandling.length() +
        this.symbol.length() +
        this.buyOrSell.length() +
        this.orderAmount.length() +
        this.orderType.length();

      return Integer.toString(bodyLength);
    }

    int bodyLength = (3 * 11) + 11 +
      this.messageType.length() +
      this.sourceID.length() +
      this.targetID.length() +
      this.messageSeqNum.length() +
      this.messageTimeSent.length();

    // TODO: Finish this.

    return Integer.toString(bodyLength);
  }

  private String getFixMessageWithoutChecksum(String messageType) {

    String header = "";
    String body = "";

    header = "8=" + this.fixVersion + "|" +
      "9=" + this.bodyLength + "|" +
      "35=" + this.messageType + "|" +
      "49=" + this.sourceID + "|" +
      "56=" + this.targetID + "|" +
      "34=" + this.messageSeqNum + "|" +
      "52=" + this.messageTimeSent + "|";

    if (messageType.equals("D")) {


    }
    else {


    }

    return header + body;
  }
}
