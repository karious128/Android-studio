package com.example.a25app1_23251109128_zgq_jellymusic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.util.Calendar;
import android.widget.DatePicker;
import android.app.DatePickerDialog;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword, etBirthday;
    private CheckBox cbRemember;
    private SharedPreferences sp;
    private static final String USER_DB_KEY = "user_database"; // 存所有用户 {user: pwd}
    private static final String REMEMBER_USER = "remember_user";
    private static final String REMEMBER_PWD = "remember_pwd";
    private static final String IS_REMEMBER = "is_remember";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        sp = getSharedPreferences("login_prefs", MODE_PRIVATE);
        loadRememberedAccount();
        setupClickListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etBirthday = findViewById(R.id.etBirthday);
        cbRemember = findViewById(R.id.cbRemember);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);
        etBirthday.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // 创建DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // 将选择的日期格式化为字符串（例如：YYYY-MM-DD）
                String selectedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth);
                etBirthday.setText(selectedDate);
            }
        }, year, month, day);

        // 显示DatePickerDialog
        datePickerDialog.show();
    }

    private void loadRememberedAccount() {
        boolean isRemember = sp.getBoolean(IS_REMEMBER, false);
        if (isRemember) {
            etUsername.setText(sp.getString(REMEMBER_USER, ""));
            etPassword.setText(sp.getString(REMEMBER_PWD, ""));
            cbRemember.setChecked(true);
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.btnLogin).setOnClickListener(v -> handleLogin());
        findViewById(R.id.btnRegister).setOnClickListener(v -> handleRegister());
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject userDb = loadUserDatabase();
        if (!userDb.has(username)) {
            Toast.makeText(this, "该用户未注册", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // 修复：正确处理两种存储方式
            Object userData = userDb.get(username);
            String savedPassword;

            if (userData instanceof String) {
                // 处理直接存储密码的用户
                savedPassword = (String) userData;
            } else if (userData instanceof JSONObject) {
                // 处理其他用户（存储为 JSONObject）
                savedPassword = ((JSONObject) userData).getString("password");
            } else {
                throw new Exception("Invalid user data type");
            }

            if (!savedPassword.equals(password)) {
                Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
                return;
            }

            // 登录成功
            if (cbRemember.isChecked()) {
                sp.edit()
                        .putBoolean(IS_REMEMBER, true)
                        .putString(REMEMBER_USER, username)
                        .putString(REMEMBER_PWD, password)
                        .apply();
            } else {
                sp.edit()
                        .putBoolean(IS_REMEMBER, false)
                        .remove(REMEMBER_USER)
                        .remove(REMEMBER_PWD)
                        .apply();
            }
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "登录异常", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleRegister() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String birthday = etBirthday.getText().toString().trim();
        if (username.isEmpty() || password.isEmpty() || birthday.isEmpty()) {
            Toast.makeText(this, "用户名、密码和生日不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject userDb = loadUserDatabase();
        if (userDb.has(username)) {
            Toast.makeText(this, "该用户名已存在", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            JSONObject user = new JSONObject();
            user.put("password", password);
            user.put("birthday", birthday);
            userDb.put(username, user);
            saveUserDatabase(userDb);
            Toast.makeText(this, "注册成功！", Toast.LENGTH_SHORT).show();
            // 可选：自动填充并勾选记住
            cbRemember.setChecked(true);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "注册失败", Toast.LENGTH_SHORT).show();
        }
    }

    private JSONObject loadUserDatabase() {
        String jsonStr = sp.getString(USER_DB_KEY, "{}");
        try {
            return new JSONObject(jsonStr);
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private void saveUserDatabase(JSONObject db) {
        sp.edit().putString(USER_DB_KEY, db.toString()).apply();
    }
}