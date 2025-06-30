package com.example.qrscanner;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    private TextView tvContentType;
    private TextView tvScannedContent;
    private Button btnCopy;
    private Button btnShare;
    private Button btnOpenLink;
    private Button btnScanAgain;
    private ImageButton btnBack;

    private String scannedContent;

    @Override
    @Override
    @Override
    @Override
    @Override
    @Override
    @Override
    @Override
    @Override
    @Override
    @Override
    @Override
    @Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
        handleScanResult();
    }

    private void initViews() {
        tvContentType = findViewById(R.id.tv_content_type);
        tvScannedContent = findViewById(R.id.tv_scanned_content);
        btnCopy = findViewById(R.id.btn_copy);
        btnShare = findViewById(R.id.btn_share);
        btnOpenLink = findViewById(R.id.btn_open_link);
        btnScanAgain = findViewById(R.id.btn_scan_again);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCopy.setOnClickListener(v -> copyToClipboard());
        btnShare.setOnClickListener(v -> shareContent());
        btnOpenLink.setOnClickListener(v -> openLink());
        btnScanAgain.setOnClickListener(v -> scanAgain());
    }

    private void handleScanResult() {
        scannedContent = getIntent().getStringExtra("scan_result");

        if (scannedContent != null && !scannedContent.isEmpty()) {
            tvScannedContent.setText(scannedContent);
            determineContentType();
        } else {
            tvScannedContent.setText("No content found");
            tvContentType.setText("Error");
        }
    }

    private void determineContentType() {
        if (scannedContent == null || scannedContent.isEmpty()) {
            tvContentType.setText("Error");
            return;
        }

        if (Patterns.WEB_URL.matcher(scannedContent).matches()) {
            tvContentType.setText("Website URL");
            btnOpenLink.setVisibility(android.view.View.VISIBLE);
        } else if (Patterns.EMAIL_ADDRESS.matcher(scannedContent).matches()) {
            tvContentType.setText("Email Address");
            btnOpenLink.setText("Send Email");
            btnOpenLink.setVisibility(android.view.View.VISIBLE);
        } else if (Patterns.PHONE.matcher(scannedContent).matches()) {
            tvContentType.setText("Phone Number");
            btnOpenLink.setText("Call Number");
            btnOpenLink.setVisibility(android.view.View.VISIBLE);
        } else if (scannedContent.startsWith("WIFI:")) {
            tvContentType.setText("WiFi Network");
            parseWifiInfo();
        } else if (scannedContent.startsWith("geo:")) {
            tvContentType.setText("Location");
            btnOpenLink.setText("Open in Maps");
            btnOpenLink.setVisibility(android.view.View.VISIBLE);
        } else if (scannedContent.startsWith("BEGIN:VCARD")) {
            tvContentType.setText("Contact Card");
            parseVCardInfo();
        } else if (scannedContent.startsWith("BEGIN:VEVENT")) {
            tvContentType.setText("Calendar Event");
        } else if (scannedContent.startsWith("smsto:") || scannedContent.startsWith("sms:")) {
            tvContentType.setText("SMS Message");
            btnOpenLink.setText("Send SMS");
            btnOpenLink.setVisibility(android.view.View.VISIBLE);
        } else {
            tvContentType.setText("Text");
        }
    }

    private void parseWifiInfo() {
        // Parse WiFi QR code format: WIFI:T:WPA;S:MyNetwork;P:MyPassword;H:false;
        try {
            String[] parts = scannedContent.split(";");
            StringBuilder wifiInfo = new StringBuilder("WiFi Network Information:\n\n");

            for (String part : parts) {
                if (part.startsWith("S:")) {
                    wifiInfo.append("Network: ").append(part.substring(2)).append("\n");
                } else if (part.startsWith("P:")) {
                    wifiInfo.append("Password: ").append(part.substring(2)).append("\n");
                } else if (part.startsWith("T:")) {
                    wifiInfo.append("Security: ").append(part.substring(2)).append("\n");
                }
            }

            tvScannedContent.setText(wifiInfo.toString());
        } catch (Exception e) {
            // Keep original content if parsing fails
        }
    }

    private void parseVCardInfo() {
        // Parse basic vCard information
        try {
            StringBuilder contactInfo = new StringBuilder("Contact Information:\n\n");
            String[] lines = scannedContent.split("\n");

            for (String line : lines) {
                if (line.startsWith("FN:")) {
                    contactInfo.append("Name: ").append(line.substring(3)).append("\n");
                } else if (line.startsWith("TEL:")) {
                    contactInfo.append("Phone: ").append(line.substring(4)).append("\n");
                } else if (line.startsWith("EMAIL:")) {
                    contactInfo.append("Email: ").append(line.substring(6)).append("\n");
                } else if (line.startsWith("ORG:")) {
                    contactInfo.append("Organization: ").append(line.substring(4)).append("\n");
                }
            }

            tvScannedContent.setText(contactInfo.toString());
        } catch (Exception e) {
            // Keep original content if parsing fails
        }
    }

    private void copyToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("QR Code Content", scannedContent);

        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareContent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, scannedContent);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "QR Code Content");

        try {
            startActivity(Intent.createChooser(shareIntent, "Share QR Code Content"));
        } catch (Exception e) {
            Toast.makeText(this, "Unable to share content", Toast.LENGTH_SHORT).show();
        }
    }

    private void openLink() {
        try {
            Intent intent = null;

            if (Patterns.WEB_URL.matcher(scannedContent).matches()) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scannedContent));
            } else if (Patterns.EMAIL_ADDRESS.matcher(scannedContent).matches()) {
                intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + scannedContent));
            } else if (Patterns.PHONE.matcher(scannedContent).matches()) {
                intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + scannedContent));
            } else if (scannedContent.startsWith("geo:")) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scannedContent));
            } else if (scannedContent.startsWith("smsto:") || scannedContent.startsWith("sms:")) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scannedContent));
            }

            if (intent != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Unable to open this content", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open this content", Toast.LENGTH_SHORT).show();
        }
    }

    private void scanAgain() {
        finish();
    }
}