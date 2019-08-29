package za.co.wethinkcode;

// This class doesn't validate and assumes that the string passed in is a valid buy
// or sell single order FIX message.

public class SingleOrderDecoder {

  private String[] splitOrder;

  public SingleOrderDecoder(String buyOrSellSingleOrder) {

    this.splitOrder = buyOrSellSingleOrder.split("|");
  }

  // Header Info
  public String getFixVersion() { return this.splitOrder[0].split("=")[1]; }
  public String getBodyLength() { return this.splitOrder[1].split("=")[1]; }
  public String getMessageType() { return this.splitOrder[2].split("=")[1]; }
  public String getSourceID() { return this.splitOrder[3].split("=")[1]; }
  public String getTargetID() { return this.splitOrder[4].split("=")[1]; }
  public String getMessageSeqNum() { return this.splitOrder[5].split("=")[1]; }
  public String getMessageTimeSent() { return this.splitOrder[6].split("=")[1]; }

  // Body Info
  public String getBrokerOrderID() { return this.splitOrder[7].split("=")[1]; }
  public String getOrderHandling() { return this.splitOrder[8].split("=")[1]; }
  public String getSymbol() { return this.splitOrder[9].split("=")[1]; }
  public String getOrderAmount() { return this.splitOrder[10].split("=")[1]; }
  public String getOrderType() { return this.splitOrder[11].split("=")[1]; }

  // Footer Info
  public String getChecksum() { return this.splitOrder[12].split("=")[1]; }
}
