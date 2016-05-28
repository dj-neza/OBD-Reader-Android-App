package mts.mts;


import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.control.DistanceMILOnCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class Main extends FragmentActivity implements LocationListener {

    //screen slider
    private static final int NUM_PAGES = 2;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    //spremenljivke
    private Location location1, location2;
    private int lat, lng = 0;
    private BluetoothAdapter BA = null;
    private LocationManager GPS;
    private LocationListener locationListener = new MyLocationListener();
    private int REQ_BT_ENABLE = 0;
    private static BluetoothSocket socket = null;
    public BluetoothDevice device = null;
    Stopwatch timer = new Stopwatch();

    private double plat,plon,clat,clon,dis;
    boolean bool=true;
    private String provider;

    private int i = 0;
    private String cas = "";
    private long deltaCas = 0;
    private long razlika = 0;
    private long cas2 = 0;

    public String speed, fuelConsp, km, RPM, coolTemp, izpAvg, start, stop, avgConsp = "";
    public int avgSpeed, maxSpeed, stMeritev, stMeritev2 = 0;
    public double avgFuelConsp = 0;

    private TextView Tspeed, TRPM, Tkm, Tfuel, Pfuel, Pspeed, Ptemp, Pcas;
    private Button start1;
    public int stKlikov = 0;

    //background obd
    private class MyAsyncTask extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            RPMCommand engineRpmCommand = new RPMCommand();
            SpeedCommand speedCommand = new SpeedCommand();
            ConsumptionRateCommand fuelConsumption = new ConsumptionRateCommand();
            DistanceMILOnCommand distance = new DistanceMILOnCommand();
            EngineCoolantTemperatureCommand engineCoolantTemp = new EngineCoolantTemperatureCommand();

            cas2 = 0;
            String noData = "no data";
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                cas = timer.toString();
                razlika = Math.abs(cas2 - deltaCas);

                if (i == 0 || razlika > 2) {
                    try {
                        engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
                        String RPM1 = engineRpmCommand.getFormattedResult();
                        int RPM1L = RPM1.length();
                        String subRPM1 = RPM1.substring(0, RPM1L-3);
                        int rpm = Integer.parseInt(subRPM1);
                        RPM = subRPM1;
                        double fl = rpm/270;
                        fuelConsp = String.format("%.2f", fl);
                        avgFuelConsp = avgFuelConsp * stMeritev2;
                        avgFuelConsp = avgFuelConsp + (int)fl;
                        stMeritev2++;
                        avgFuelConsp = avgFuelConsp / stMeritev2;
                        avgConsp = String.format("%.2f", avgFuelConsp);
                    } catch (IOException e) {
                        RPM = noData;
                    } catch (InterruptedException e) {
                        //Log.d("OBD", "RPMe2");
                    }
                    try {
                        speedCommand.run(socket.getInputStream(), socket.getOutputStream());
                        String speed1 = speedCommand.getFormattedResult();
                        int speed1L = speed1.length();
                        String subspeed1 = speed1.substring(0, speed1L-4);
                        int hitrost = Integer.parseInt(subspeed1);
                        speed = subspeed1;
                        if (hitrost > maxSpeed) {
                            maxSpeed = hitrost;
                        }
                        avgSpeed = avgSpeed * stMeritev;
                        avgSpeed = avgSpeed + hitrost;
                        stMeritev++;
                        avgSpeed = avgSpeed / stMeritev;
                        izpAvg = Integer.toString(avgSpeed);
                    } catch (IOException e) {
                        speed = noData;
                        izpAvg = noData;
                    } catch (InterruptedException e) {
                        //Log.d("OBD", "eSPEEDe2");
                    }

                    /* FUEL CONSUMPTION
                    try {
                        fuelConsumption.run(socket.getInputStream(), socket.getOutputStream());
                        fuelConsp = fuelConsumption.getFormattedResult();
                        povpFuel = Integer.parseInt(fuelConsp);
                        avgFuelConsp = avgFuelConsp * stMeritev2;
                        avgFuelConsp = avgFuelConsp + povpFuel;
                        stMeritev2++;
                        avgFuelConsp = avgFuelConsp / stMeritev2;
                        avgConsp = Integer.toString(avgFuelConsp);
                    } catch (IOException e) {
                        avgConsp = noData;
                        fuelConsp = noData;
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                        Log.d("OBD", "FUELe2");
                    }*/
                    //DISTANCE
                    try {
                        distance.run(socket.getInputStream(), socket.getOutputStream());
                        String km1 = distance.getFormattedResult();
                        int km1L = km1.length();
                        String subkm1 = km1.substring(0, km1L-2);
                        km = subkm1;
                    } catch (IOException e) {
                        km = noData;
                    } catch (InterruptedException e) {
                        km = noData;
                    }
                    try {
                        engineCoolantTemp.run(socket.getInputStream(), socket.getOutputStream());
                        String cool1 = engineCoolantTemp.getFormattedResult();
                        int cool1L = cool1.length();
                        String subcool1 = cool1.substring(0, cool1L-1);
                        coolTemp = subcool1;
                    } catch (IOException e) {
                        coolTemp = noData;
                    } catch (InterruptedException e) {
                        coolTemp = noData;
                    }
                    i++;
                }
                publishProgress();
                if (razlika > 3) cas2 = deltaCas;
                deltaCas = timer.getElapsedTimeSecs();

                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    //return;
                }
                try {
                    location2 = GPS.getLastKnownLocation(provider);
                    if (location2 != null) {
                        clat = location2.getLatitude();
                        clon = location2.getLongitude();
                        plat = location1.getLatitude();
                        plon = location1.getLongitude();
                        if (clat != plat || clon != plon) {
                            dis += getDistance(plat, plon, clat, clon);
                            plat = clat;
                            plon = clon;
                        }
                    }
                } catch(Exception e) {}
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            start1.setText(cas);
            Pcas.setText(cas);
            TRPM.setText(RPM);
            Tspeed.setText(speed);
            Pspeed.setText(izpAvg);
            Tfuel.setText(fuelConsp);
            Pfuel.setText(avgConsp);
            Ptemp.setText(coolTemp);
            Tkm.setText(km);
        }

    }

    //GPS
    public double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double latA = Math.toRadians(lat1);
        double latB = Math.toRadians(lat2);
        double lonA = Math.toRadians(lon1);
        double lonB = Math.toRadians(lon2);

        double cosAg = (Math.cos(latA) * Math.cos(latB) * Math.cos(lonB-lonA)) + (Math.sin(latA) * Math.sin(latB));
        double ang = Math.cos(cosAg);
        double dist = ang * 6371;
        return dist;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slider);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        //BLUETOOTH
        ArrayList deviceStrs = new ArrayList();
        final ArrayList devices = new ArrayList();
        BA = BluetoothAdapter.getDefaultAdapter();
        if (BA == null) {
            Log.i("BA", "NE DELA");
        }
        bluetoothOn();
        while (!BA.isEnabled()) ;
        Set<BluetoothDevice> pairedDevices = BA.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceStrs.add(device.getName() + "\n" + device.getAddress());
                devices.add(device.getAddress());
            }
        }
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice,
                deviceStrs.toArray(new String[deviceStrs.size()]));
        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                String deviceAddress = (String) devices.get(position);
                BluetoothAdapter BA = BluetoothAdapter.getDefaultAdapter();
                device = BA.getRemoteDevice(deviceAddress);
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                try {
                    socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                    socket.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        alertDialog.setTitle("Choose Bluetooth device");
        alertDialog.show();

        //GPS
        GPS = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        boolean enabled = GPS.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.i("GPS", Boolean.toString(enabled));
        if (!enabled) {
            Log.i("GPS", "GPS NI ENABLED");
        } else {
            Log.i("GPS", "JE ENABLED");
        }
        provider = (LocationManager.GPS_PROVIDER);
        location1 = GPS.getLastKnownLocation(provider);
    }

    // slider
    // @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
                super(fm);
            }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new ScreenSlideFragment();
            } else return new ScreenSlideFragment2();
        }

        @Override
        public int getCount() {
                return NUM_PAGES;
            }
    }

    //BLUETOOTH METODE
    public void bluetoothOn() {
        if (BA.isEnabled() == false) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, REQ_BT_ENABLE);
            Log.i("BA", "online");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_BT_ENABLE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "BlueTooth is now Enabled", Toast.LENGTH_LONG).show();
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Error occured while enabling. Leaving the application.", Toast.LENGTH_LONG).show();
                Log.i("BA", "offline");
                finish();
                System.exit(0);
            }
        }
    }

    public void klik(View view) {
        start1 = (Button) findViewById(R.id.start1);
        Tspeed = (TextView) findViewById(R.id.textView3);
        TRPM = (TextView) findViewById(R.id.textView11);
        Tkm = (TextView) findViewById(R.id.textView13);
        Tfuel = (TextView) findViewById(R.id.textView5);
        Pfuel = (TextView) findViewById(R.id.textView32);
        Pspeed = (TextView) findViewById(R.id.textView31);
        Ptemp = (TextView) findViewById(R.id.textView30);
        Pcas = (TextView) findViewById(R.id.textView36);

        timer.start();

            //OBD kar rabimo na zacetku
            try {
                new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
            } catch (Exception e) {
                System.out.print("Failed echoOffComand");
                Log.i("OBD", "Failed echoOffComand");
            }
            try {
                new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
            } catch (Exception e) {
                System.out.print("Failed lineFeedOffCommand");
                Log.i("OBD", "Failed lineFeedOffCommand");
            }
            try {
                new TimeoutCommand(125).run(socket.getInputStream(), socket.getOutputStream());
            } catch (Exception e) {
                System.out.print("Failed timeOutCommand");
                Log.i("OBD", "Failed timeOutCommand");
            }
            try {
                new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());
            } catch (Exception e) {
                System.out.print("Failed selectProtocolCommand");
                Log.i("OBD", "Failed selectProtocolCommand");
            }
            try {
                new AmbientAirTemperatureCommand().run(socket.getInputStream(), socket.getOutputStream());
            } catch (Exception e) {
                Log.i("OBD", "Failed ambientAirTempCom");
            }
            MyAsyncTask myAsyncTask = new MyAsyncTask();
            myAsyncTask.execute();
        }

    //GPS metode
    @Override
    public void onLocationChanged(Location location) {
        lat = (int) (location.getLatitude());
        lng = (int) (location.getLongitude());
    }

    // for setting mock location
    public static Location getMockLocation(double latitude, double longitude) {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

}


