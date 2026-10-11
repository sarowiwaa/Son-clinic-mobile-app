package ke.sonclinic.mobile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
 private ClinicDb db; private LinearLayout root,body; private final int navy=0xFF10243A,blue=0xFF176B87,pale=0xFFF3F6FA;
 @Override protected void onCreate(Bundle b){super.onCreate(b);db=new ClinicDb(this);showLogin();}
 private void showLogin(){
  LinearLayout out=new LinearLayout(this);out.setGravity(Gravity.CENTER);out.setOrientation(1);out.setPadding(26,26,26,26);out.setBackgroundColor(navy);
  TextView logo=text("SON",38,0xFFFFFFFF);logo.setGravity(Gravity.CENTER);
  TextView title=text("CLINIC & PHARMACY",18,0xFFFFFFFF);title.setGravity(Gravity.CENTER);
  TextView sub=text("Management System",14,0xFFB9D0E0);sub.setGravity(Gravity.CENTER);
  EditText user=field("Username");user.setText("admin");EditText pass=field("Demo password");pass.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
  Button login=button("Sign in");login.setOnClickListener(v->{if(user.getText().toString().trim().equals("admin")&&pass.getText().toString().equals("ChangeMe123!"))showApp();else new AlertDialog.Builder(this).setTitle("Demo login").setMessage("Use admin / ChangeMe123!. This hard-coded login is for demonstration only.").setPositiveButton("OK",null).show();});
  out.addView(logo,params(-1,-2));out.addView(title,params(-1,-2));out.addView(sub,params(-1,-2));space(out,22);out.addView(user,params(-1,-2));out.addView(pass,params(-1,-2));out.addView(login,params(-1,-2));setContentView(out);
 }
 private void showApp(){
  root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(pale);
  LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(14,10,14,10);top.setBackgroundColor(navy);
  TextView brand=text("SON | Clinic & Pharmacy",18,0xFFFFFFFF);top.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
  Button logout=button("Logout");logout.setOnClickListener(v->showLogin());top.addView(logout);root.addView(top);
  ScrollView scroll=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(1);body.setPadding(16,16,16,18);scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  HorizontalScrollView hs=new HorizontalScrollView(this);LinearLayout nav=new LinearLayout(this);nav.setBackgroundColor(0xFFFFFFFF);
  for(String p:new String[]{"Dashboard","Patients","Appointments","Inventory","Sales"}){Button b=button(p);b.setOnClickListener(v->open(p));nav.addView(b);}hs.addView(nav);root.addView(hs);setContentView(root);open("Dashboard");
 }
 private void open(String p){body.removeAllViews();switch(p){case "Dashboard":dashboard();break;case "Patients":patients();break;case "Appointments":appointments();break;case "Inventory":inventory();break;case "Sales":sales();break;}}
 private void dashboard(){heading("Dashboard","Clinic and pharmacy overview");stat("Registered patients",db.count("patients")+"");stat("Appointments",db.count("appointments")+"");stat("Medicine items",db.count("medicines")+"");stat("Today's sales",kes(db.todaySales()));TextView n=text("Use the navigation to manage patients, appointments, medicine stock and pharmacy sales. Data is stored locally on this device.",14,navy);n.setPadding(14,14,14,14);n.setBackgroundColor(-1);body.addView(n);TextView warn=text("Demo only: do not enter real patient information until security, backup and privacy safeguards are implemented.",13,0xFF8A5A00);body.addView(warn);}
 private void patients(){
  heading("Patients","Register and search patient records");EditText name=field("Full name *"),phone=field("Phone number"),gender=field("Gender"),dob=field("Date of birth YYYY-MM-DD"),notes=field("Notes (optional)");Button save=button("Register patient");
  save.setOnClickListener(v->{if(name.getText().toString().trim().isEmpty()){toast("Enter patient's name");return;}String no="P-"+System.currentTimeMillis();try{db.addPatient(no,name.getText().toString().trim(),phone.getText().toString().trim(),gender.getText().toString().trim(),dob.getText().toString().trim(),notes.getText().toString().trim());toast("Registered: "+no);open("Patients");}catch(Exception e){toast("Save failed: "+e.getMessage());}});
  fields(name,phone,gender,dob,notes,save);EditText search=field("Search name, phone or patient number");Button find=button("Search");find.setOnClickListener(v->showPatients(search.getText().toString().trim()));fields(search,find);showPatients("");
 }
 private void showPatients(String q){Cursor c=db.query("SELECT patient_no,name,phone,gender,dob FROM patients WHERE name LIKE ? OR phone LIKE ? OR patient_no LIKE ? ORDER BY id DESC",new String[]{"%"+q+"%","%"+q+"%","%"+q+"%"});try{while(c.moveToNext())body.addView(card(c.getString(0)+"\n"+c.getString(1)+" • "+safe(c.getString(2))+"\n"+safe(c.getString(3))+" • "+safe(c.getString(4))));}finally{c.close();}}
 private void appointments(){
  heading("Appointments","Schedule and update visits");EditText patientNo=field("Patient number P-..."),date=field("Appointment date YYYY-MM-DD"),reason=field("Reason / department");Button save=button("Schedule appointment");
  save.setOnClickListener(v->{Cursor c=db.query("SELECT id,name FROM patients WHERE patient_no=?",new String[]{patientNo.getText().toString().trim()});try{if(!c.moveToFirst()){toast("Patient not found; register first");return;}if(date.getText().toString().trim().isEmpty()){toast("Enter appointment date");return;}if(db.addAppointment(c.getInt(0),c.getString(1),date.getText().toString().trim(),reason.getText().toString().trim())>0){toast("Appointment scheduled");open("Appointments");}}finally{c.close();}});
  fields(patientNo,date,reason,save);Cursor c=db.query("SELECT id,patient_name,appt_date,reason,status FROM appointments ORDER BY appt_date DESC",null);try{while(c.moveToNext()){int id=c.getInt(0);LinearLayout row=card(c.getString(1)+"\n"+c.getString(2)+" • "+safe(c.getString(3))+"\nStatus: "+c.getString(4));Button done=button("Complete"),cancel=button("Cancel");done.setOnClickListener(v->{db.updateAppointmentStatus(id,"COMPLETED");open("Appointments");});cancel.setOnClickListener(v->{db.updateAppointmentStatus(id,"CANCELLED");open("Appointments");});row.addView(done);row.addView(cancel);body.addView(row);}}finally{c.close();}
 }
 private void inventory(){
  heading("Pharmacy inventory","Manage stock, price and expiry");EditText name=field("Medicine name *"),cat=field("Category"),qty=numField("Quantity"),price=numField("Unit price KES"),expiry=field("Expiry YYYY-MM-DD"),reorder=numField("Low stock threshold");qty.setText("0");price.setText("0");reorder.setText("10");Button add=button("Add medicine");
  add.setOnClickListener(v->{try{if(name.getText().toString().trim().isEmpty()){toast("Enter medicine name");return;}int q=Integer.parseInt(qty.getText().toString()),r=Integer.parseInt(reorder.getText().toString());double p=Double.parseDouble(price.getText().toString());if(q<0||r<0||p<0)throw new Exception();if(db.addMedicine(name.getText().toString().trim(),cat.getText().toString().trim(),q,p,expiry.getText().toString().trim(),r)>0){toast("Medicine added");open("Inventory");}}catch(Exception e){toast("Check quantity, price and reorder level");}});
  fields(name,cat,qty,price,expiry,reorder,add);Cursor c=db.query("SELECT id,name,category,quantity,price,expiry,reorder_level FROM medicines ORDER BY name",null);try{while(c.moveToNext()){int id=c.getInt(0),stock=c.getInt(3),threshold=c.getInt(6);LinearLayout row=card(c.getString(1)+"\n"+safe(c.getString(2))+" • Stock: "+stock+"\nKES "+String.format(Locale.US,"%.2f",c.getDouble(4))+" • Expiry: "+safe(c.getString(5))+"\n"+(stock<=threshold?"LOW STOCK":"In stock"));Button adjust=button("+ Stock");adjust.setOnClickListener(v->adjustDialog(id));row.addView(adjust);body.addView(row);}}finally{c.close();}
 }
 private void adjustDialog(int id){EditText amount=numField("Units to add");amount.setText("1");new AlertDialog.Builder(this).setTitle("Adjust stock").setView(amount).setPositiveButton("Add",(d,w)->{try{int n=Integer.parseInt(amount.getText().toString());if(!db.adjustStock(id,n))throw new Exception();open("Inventory");}catch(Exception e){toast("Enter positive whole number");}}).setNegativeButton("Cancel",null).show();}
 private void sales(){
  heading("Pharmacy sales","Record sale and deduct stock");Cursor c=db.query("SELECT id,name,quantity,price FROM medicines WHERE quantity>0 ORDER BY name",null);ArrayList<Integer> ids=new ArrayList<>(),stocks=new ArrayList<>();ArrayList<Double> prices=new ArrayList<>();ArrayList<String> labels=new ArrayList<>();
  try{while(c.moveToNext()){ids.add(c.getInt(0));stocks.add(c.getInt(2));prices.add(c.getDouble(3));labels.add(c.getString(1)+" • stock "+c.getInt(2)+" • KES "+String.format(Locale.US,"%.2f",c.getDouble(3)));}}finally{c.close();}
  if(labels.isEmpty()){body.addView(text("No medicines in stock. Add inventory first.",15,navy));return;}
  Spinner<String> pick=new Spinner<>(this);pick.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,labels));EditText amount=numField("Quantity to sell");amount.setText("1");Button sell=button("Record sale");
  sell.setOnClickListener(v->{try{int i=pick.getSelectedItemPosition(),q=Integer.parseInt(amount.getText().toString());String med=labels.get(i).split(" • ")[0];if(db.sellMedicine(ids.get(i),med,q,prices.get(i),stocks.get(i))){toast("Sale saved: "+kes(q*prices.get(i)));open("Sales");}else toast("Insufficient stock or invalid quantity; refresh and retry");}catch(Exception e){toast("Enter a valid quantity");}});
  fields(pick,amount,sell);body.addView(text("Today's sales: "+kes(db.todaySales()),17,blue));Cursor s=db.query("SELECT medicine_name,quantity,unit_price,total,sold_at FROM sales ORDER BY id DESC LIMIT 100",null);try{while(s.moveToNext())body.addView(card(s.getString(0)+"\nQty: "+s.getInt(1)+" • Unit: "+kes(s.getDouble(2))+"\nTotal: "+kes(s.getDouble(3))+"\n"+safe(s.getString(4))));}finally{s.close();}
 }
 private void heading(String a,String b){TextView h=text(a,26,navy);h.setTypeface(null,1);body.addView(h);body.addView(text(b,14,0xFF718096));space(body,12);}
 private void stat(String title,String val){LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(14,12,14,12);box.setBackgroundColor(-1);box.addView(text(title.toUpperCase(Locale.ROOT),12,0xFF718096));TextView v=text(val,26,blue);v.setTypeface(null,1);box.addView(v);LinearLayout.LayoutParams lp=params(-1,-2);lp.bottomMargin=9;body.addView(box,lp);}
 private LinearLayout card(String s){LinearLayout x=new LinearLayout(this);x.setOrientation(1);x.setPadding(14,12,14,12);x.setBackgroundColor(-1);TextView t=text(s,14,navy);x.addView(t);LinearLayout.LayoutParams lp=params(-1,-2);lp.bottomMargin=8;x.setLayoutParams(lp);return x;}
 private void fields(View... vs){for(View v:vs){LinearLayout.LayoutParams lp=params(-1,-2);lp.bottomMargin=7;body.addView(v,lp);}}
 private EditText field(String hint){EditText e=new EditText(this);e.setHint(hint);e.setSingleLine(true);e.setTextSize(15);e.setPadding(12,8,12,8);e.setBackgroundColor(-1);return e;}
 private EditText numField(String hint){EditText e=field(hint);e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);return e;}
 private Button button(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(-1);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(blue));return b;}
 private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
 private LinearLayout.LayoutParams params(int w,int h){return new LinearLayout.LayoutParams(w,h);}
 private void space(LinearLayout l,int h){View v=new View(this);l.addView(v,new LinearLayout.LayoutParams(1,h));}
 private String kes(double v){return "KES "+String.format(Locale.US,"%,.2f",v);}
 private String safe(String s){return s==null||s.trim().isEmpty()?"—":s;}
 private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}
