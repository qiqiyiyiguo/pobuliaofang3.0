<template>
  <div class="container">
    <div class="from-box">
      <!-- 注册 -->
      <div class="register-box hidden">
        <h1>register</h1>
        <input type="text" placeholder="用户名" />
        <input type="email" placeholder="邮箱" />
        <input type="password" placeholder="密码" />
        <input type="password" placeholder="确认密码" />
        <button>注册</button>
      </div>
      <!-- 登录 -->
      <div class="login-box">
        <h1>login</h1>
        <input type="text" placeholder="用户名" />
        <input type="password" placeholder="密码" />
        <button>登录</button>
      </div>
    </div>
    <div class="con-box left">
      <h2>欢迎来到<span>AI模拟面试官</span></h2>
      <p>快来体验你的专属<span>面试官</span>吧</p> 
      <img src="../../assets/image/注册.png" alt="" />
      <p>已有账号</p>
      <button id="login">去登录</button>
    </div>
    <div class="con-box right">
      <h2>欢迎来到<span>AI模拟面试官</span></h2>
      //<p>快来体验你的专属<span>面试官</span>吧</p>
      <img src="../../assets/image/登录.png" alt="" />
      <p>没有账号?</p>
      <button id="register">去注册</button>
    </div>
  </div>
</template>

<script>
export default {
  name: "Login",
  mounted() {
    // 将原本的 JS 逻辑放在 mounted 生命周期中
    const loginBtn = document.getElementById("login");
    const registerBtn = document.getElementById("register");
    const formBox = document.querySelector(".from-box");
    const registerBox = document.querySelector(".register-box");
    const loginBox = document.querySelector(".login-box");

    if (registerBtn) {
      registerBtn.addEventListener("click", () => {
        formBox.style.transform = "translateX(90%)";
        loginBox.classList.add("hidden");
        registerBox.classList.remove("hidden");
      });
    }

    if (loginBtn) {
      loginBtn.addEventListener("click", () => {
        formBox.style.transform = "translateX(-10%)";
        registerBox.classList.add("hidden");
        loginBox.classList.remove("hidden");
      });
    }
  },
};
</script>

<style>
/* 原 index.css 内容直接复制过来 */
* {
  padding: 0;
  margin: 0;
}

body {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(200deg, #f3e7e9, #e3eeff);
}

.container {
  background-color: #fff;
  width: 650px;
  height: 415px;
  box-shadow: 5px 5px 5px rgba(0, 0, 0, 0.2);
  position: absolute;
  top: 50%;
  left: 60%;
  transform: translate(-50%, -50%);
}

.from-box {
  position: absolute;
  top: -10%;
  left: 0%;
  background-color: #d3b7d8;
  width: 340px;
  height: 500px;
  border-radius: 5px;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
  transition: 0.5s ease-in-out;
}

.register-box,
.login-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.hidden {
  display: none;
  transition: 0.5s;
}

h1 {
  text-align: center;
  margin-bottom: 25px;
  text-transform: uppercase;
  color: #fff;
  letter-spacing: 5px;
}

input {
  background-color: transparent;
  width: 70%;
  color: #fff;
  border: none;
  border-bottom: 1px solid rgba(255, 255, 255, 0.4);
  padding: 10px 0;
  text-indent: 10px;
  margin: 8px 0;
  font-size: 14px;
  letter-spacing: 2px;
}

input::placeholder {
  color: #fff;
}

input:focus {
  color: #a262ad;
  outline: none;
  border-bottom: 1px solid #a262ad80;
  transition: 0.5s;
}

input:focus::placeholder {
  opacity: 0;
}

.from-box button {
  width: 70%;
  margin-top: 35px;
  background-color: #f6f6f6;
  outline: none;
  border-radius: 8px;
  padding: 13px;
  color: #a262ad;
  letter-spacing: 2px;
  border: none;
  cursor: pointer;
}

.from-box button:hover {
  background-color: #a262ad;
  color: #f6f6f6;
  transition: background-color 0.5s ease;
}

.con-box {
  width: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
}

.con-box.left {
  left: -1%;
}

.con-box.right {
  right: -2%;
}

.con-box h2 {
  color: #8e9aaf;
  font-size: 25px;
  font-weight: bold;
  letter-spacing: 3px;
  text-align: center;
  margin-bottom: 4px;
}

.con-box p {
  font-size: 12px;
  letter-spacing: 2px;
  color: #8e9aaf;
  text-align: center;
}

.con-box span {
  color: #d3b7d8;
}

.con-box img {
  width: 150px;
  height: 150px;
  opacity: 0.9;
  margin: 40px 0px;
}

.con-box button {
  margin-top: 3%;
  background-color: #fff;
  color: #a262ad;
  border: 1px solid #d3b7d8;
  padding: 6px 10px;
  letter-spacing: 1px;
  outline: none;
  cursor: pointer;
}

.con-box button:hover {
  background-color: #a262ad;
  color: #f6f6f6;
}
</style>