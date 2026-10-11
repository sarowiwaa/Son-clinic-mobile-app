package ke.sonclinic.mobile;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ClinicDb extends SQLiteOpenHelper {
 public ClinicDb(Context c){super(c,"son_clinic_mobile.db",null,1);}
 @Override public void onCreate(SQLiteDatabase db){
  db.execSQL("CREATE TABLE patients(id INTEGER PRIMARY KEY AUTOINCREMENT, patient_no TEXT UNIQUE, name TEXT NOT NULL, phone TEXT, gender TEXT, dob TEXT, notes TEXT)");
  db.execSQL("CREATE TABLE appointments(id INTEGER PRIMARY KEY AUTOINCREMENT, patient_id INTEGER NOT NULL, patient_name TEXT NOT NULL, appt_date TEXT NOT NULL, reason TEXT, status TEXT DEFAULT 'SCHEDULED')");
  db.execSQL("CREATE TABLE medicines(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, category TEXT, quantity INTEGER NOT NULL DEFAULT 0, price REAL NOT NULL DEFAULT 0, expiry TEXT, reorder_level INTEGER DEFAULT 10)");
  db.execSQL("CREATE TABLE sales(id INTEGER PRIMARY KEY AUTOINCREMENT, medicine_id INTEGER NOT NULL, medicine_name TEXT NOT NULL, quantity INTEGER NOT NULL, unit_price REAL NOT NULL, total REAL NOT NULL, sold_at TEXT DEFAULT CURRENT_TIMESTAMP)");
 }
 @Override public void onUpgrade(SQLiteDatabase db,int oldV,int newV){}
 public long addPatient(String no,String name,String phone,String gender,String dob,String notes){
  ContentValues v=new ContentValues();v.put("patient_no",no);v.put("name",name);v.put("phone",phone);v.put("gender",gender);v.put("dob",dob);v.put("notes",notes);return getWritableDatabase().insertOrThrow("patients",null,v);
 }
 public long addAppointment(int id,String name,String date,String reason){
  ContentValues v=new ContentValues();v.put("patient_id",id);v.put("patient_name",name);v.put("appt_date",date);v.put("reason",reason);return getWritableDatabase().insert("appointments",null,v);
 }
 public long addMedicine(String name,String category,int qty,double price,String expiry,int reorder){
  ContentValues v=new ContentValues();v.put("name",name);v.put("category",category);v.put("quantity",qty);v.put("price",price);v.put("expiry",expiry);v.put("reorder_level",reorder);return getWritableDatabase().insert("medicines",null,v);
 }
 public boolean sellMedicine(int id,String name,int qty,double price,int available){
  if(qty<=0||qty>available)return false;SQLiteDatabase db=getWritableDatabase();db.beginTransaction();
  try{
   ContentValues st=new ContentValues();st.put("quantity",available-qty);
   if(db.update("medicines",st,"id=? AND quantity>=?",new String[]{""+id,""+qty})!=1)return false;
   ContentValues sale=new ContentValues();sale.put("medicine_id",id);sale.put("medicine_name",name);sale.put("quantity",qty);sale.put("unit_price",price);sale.put("total",qty*price);db.insertOrThrow("sales",null,sale);db.setTransactionSuccessful();return true;
  }finally{db.endTransaction();}
 }
 public int count(String table){
  if(!table.matches("patients|appointments|medicines|sales"))return 0;
  Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM "+table,null);try{return c.moveToFirst()?c.getInt(0):0;}finally{c.close();}
 }
 public double todaySales(){Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(total),0) FROM sales WHERE date(sold_at)=date('now','localtime')",null);try{return c.moveToFirst()?c.getDouble(0):0;}finally{c.close();}}
 public Cursor query(String sql,String[] args){return getReadableDatabase().rawQuery(sql,args);}
 public boolean updateAppointmentStatus(int id,String status){ContentValues v=new ContentValues();v.put("status",status);return getWritableDatabase().update("appointments",v,"id=?",new String[]{""+id})>0;}
 public boolean adjustStock(int id,int n){if(n<=0)return false;getWritableDatabase().execSQL("UPDATE medicines SET quantity=quantity+? WHERE id=?",new Object[]{n,id});return true;}
}
