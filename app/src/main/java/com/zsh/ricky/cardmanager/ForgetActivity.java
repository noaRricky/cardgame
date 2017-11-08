package com.zsh.ricky.cardmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ForgetActivity extends AppCompatActivity {

    private TextView tvQuesion;
    private EditText etAnswer;
    private Button btYes, btCancal;   //确定，取消按钮
    private String userID;
    private String question, answer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);

        initItem();

        tvQuesion.setText(question);
        btCancal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgetActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btYes.setOnClickListener(new YesOnClickListener());
    }

    private void initItem()
    {
        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
        question = intent.getStringExtra("question");
        answer = intent.getStringExtra("answer");
        tvQuesion = (TextView) this.findViewById(R.id.fg_question);
        etAnswer = (EditText) this.findViewById(R.id.fg_answer);
        btCancal = (Button) this.findViewById(R.id.fg_btCancel);
        btYes = (Button) this.findViewById(R.id.fg_btYes);
    }

    private class YesOnClickListener implements View.OnClickListener
    {

        @Override
        public void onClick(View v) {
            if (validate()) {
                String myAnswer = etAnswer.getText().toString();
                if (answer.equals(myAnswer)) {
                    Intent intent = new Intent(ForgetActivity.this, ChangeActivity.class);
                    intent.putExtra("userID", userID);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "密保答案不正确！",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * 判断密保答案是否为空
     * @return 空返回false,否则返回true
     */
    private boolean validate()
    {
        String answer = etAnswer.getText().toString().trim();
        if (answer.equals(""))
        {
            Toast.makeText(getApplicationContext(), "密保答案不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
