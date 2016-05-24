package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.Interfaces.IAccountObject;
import de.bitshares_munich.Interfaces.IAssetObject;
import de.bitshares_munich.Interfaces.ITransactionObject;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Helper;

/**
 * Created by Syed Muhammad Muzzammil on 5/17/16.
 */
public class PaymentRecieved extends Activity implements ITransactionObject,IAccountObject,IAssetObject {
    Application application = new Application();
    String receiver_id;
    String sender_id;
    JSONObject amountObj;
    JSONObject feeObj;

    @Bind(R.id.btnOk)
    Button btnOk;
    @Bind(R.id.tvFrom)
    TextView tvFrom;
    @Bind(R.id.tvTo)
    TextView tvTo;
    @Bind(R.id.tvMainAmount)
    TextView tvMainAmount;
    @Bind(R.id.tvMainAsset)
    TextView tvMainAsset;
    @Bind(R.id.tvAmount)
    TextView tvAmount;
    @Bind(R.id.tvFee)
    TextView tvFee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_recieved);
        ButterKnife.bind(this);
        application.registerTransactionObject(this);
        application.registerAccountObjectCallback(this);
        application.registerAssetObjectCallback(this);
        String block = getIntent().getStringExtra("block");
        receiver_id = getIntent().getStringExtra("receiver_id");
        sender_id = getIntent().getStringExtra("sender_id");
        getAccountObject();
        getTransactionObject(block);




    }
    @OnClick(R.id.backbutton)
    void onBackButtonPressed(){
        super.onBackPressed();
    }
    @OnClick(R.id.btnOk)
    void onOkPressed(){
        Intent intent = new Intent(getApplicationContext(), TabActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    public void getAccountObject() {
        if (Application.webSocketG.isOpen()) {
            int db_identifier = Helper.fetchIntSharePref(getApplicationContext(),getString(R.string.database_indentifier));
            String params = "{\"id\":13,\"method\":\"call\",\"params\":["+db_identifier+",\"get_objects\",[[\""+sender_id+"\",\""+receiver_id+"\"],0]]}";
            Application.webSocketG.send(params);
        }
    }
    public void getTransactionObject(String block) {
        if (Application.webSocketG.isOpen()) {
            int db_identifier = Helper.fetchIntSharePref(getApplicationContext(),getString(R.string.database_indentifier));
            String params = "{\"id\":12,\"method\":\"call\",\"params\":["+db_identifier+",\"get_transaction\",[\""+block+"\",0]]}";
            Application.webSocketG.send(params);
        }
    }
    public void getAssetObject(String amountAsset, String feeAsset) {
        if (Application.webSocketG.isOpen()) {
            int db_identifier = Helper.fetchIntSharePref(getApplicationContext(),getString(R.string.database_indentifier));
            String params = "{\"id\":14,\"method\":\"call\",\"params\":["+db_identifier+",\"get_objects\",[[\""+amountAsset+"\",\""+feeAsset+"\"],0]]}";
            Application.webSocketG.send(params);
        }
    }
    @Override
    public void accountObjectCallback(JSONObject jsonObject){
        try {
            JSONArray resultArr = (JSONArray) jsonObject.get("result");
            for (int i = 0; i < resultArr.length(); i++) {
                final JSONObject resultObj = (JSONObject) resultArr.get(i);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            if (resultObj.get("id").equals(sender_id)){
                                tvFrom.setText(resultObj.get("name").toString());
                            }else if (resultObj.get("id").equals(receiver_id)){
                                tvTo.setText(resultObj.get("name").toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void checkTransactionObject(JSONObject jsonObject){
        try {
            JSONObject result = (JSONObject) jsonObject.get("result");
            JSONArray operations = (JSONArray) result.get("operations");
            JSONArray operationsInner = (JSONArray) operations.get(0);
            JSONObject resultObj = (JSONObject) operationsInner.get(1);
            amountObj = (JSONObject) resultObj.get("amount");
            feeObj = (JSONObject) resultObj.get("fee");
            getAssetObject(amountObj.get("asset_id").toString(),feeObj.get("asset_id").toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void assetObjectCallback(JSONObject jsonObject){
        try {
            JSONArray resultArr = (JSONArray) jsonObject.get("result");
            for (int i = 0; i < resultArr.length(); i++) {
                final JSONObject resultObj = (JSONObject) resultArr.get(i);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            if (resultObj.get("id").equals(amountObj.get("asset_id").toString())){
                                Double amount = Double.parseDouble(amountObj.get("amount").toString());
                                amount = amount / Math.pow(10, Integer.parseInt(resultObj.get("precision").toString()));
                                tvMainAmount.setText(String.format("%.4f",amount));
                                tvMainAsset.setText(resultObj.get("symbol").toString());
                                tvAmount.setText(String.format("%.4f",amount)+resultObj.get("symbol").toString());
                            }
                            if (resultObj.get("id").equals(feeObj.get("asset_id").toString())){
                                Double fee = Double.parseDouble(feeObj.get("amount").toString());
                                fee = fee / Math.pow(10, Integer.parseInt(resultObj.get("precision").toString()));
                                tvFee.setText(String.format("%.4f",fee)+resultObj.get("symbol").toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
