# At2Someone
> 灵感来源于某服务器五周目（2021）时的一个 ***（我很喜欢但是断更了的）*** Bukkit 插件 遂写之。
> 
> 权当 Java 练手 欢迎大佬批评。

一个可以在 bukkit 服务器内 @人的插件。在聊天栏中 `@ + 用户名` 或直接输入用户名即可在公屏中提及某人。且附带勿扰模式和显示昵称功能。

## 兼容性
1. Bukkit 服务端
2. 游戏版本 ≥ 1.18

目前仅支持 Minecraft 1.18+ 的插件端 如 Spigot、Paper 等兼容 Bukkit API 的服务端。

经测试插件可运行于 Spigot 1.18、Mohist 1.18.2,1.20.1、Leaves 1.20.6 ~~理论上别的应该也可以~~，目前仅支持中文。

## 指令
![](https://s21.ax1x.com/2025/11/24/pZk53Us.png)

| 指令      | 权限 | 用途 |
| --------- | ----- | ----- |
| /at | `N/A` | 可用指令+插件状态查询 |
| /at dnd on/off | `at.dnd (默认就有)` | 勿扰模式(可见下方解释) |
| /at isprefix on/off | `at.admin` | 是否需要输入@才能提及人(可见下方解释) |
| /at enable | `at.admin` | 使能插件功能 |
| /at disable | `at.admin` | 禁用插件功能 |
| /at reload | `at.admin` | 重载插件配置文件 |

## 功能
#### 勿扰模式
![](https://s21.ax1x.com/2025/11/24/pZk585n.png)

当玩家输入 `/at dnd on` 时，玩家将不会收到当其他玩家提及时所发出的提示音与标题，仅会在聊天栏中高亮用户名。

此模式可通过 `/at dnd off` 进行关闭

#### isPrefix
![](https://s21.ax1x.com/2025/11/24/pZk5JCq.png)

当 OP 输入 `/at isprefix on` 时，玩家必须要输入@+用户名才可提及某人。

当 OP 输入  `/at isprefix off` 时，玩家仅需输入用户名即可提及某人。

## 效果图
![](https://s21.ax1x.com/2025/11/24/pZk53Us.png)

## 协议
MIT License
###### 必须MIT！
