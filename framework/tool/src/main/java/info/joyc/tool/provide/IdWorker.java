package info.joyc.tool.provide;

/**
 * info.joyc.util.provide.IdWorker.java
 * ==============================================
 * Copy right 2015-2018 by http://www.joyc.info
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 * <p>
 * 类snowflake方案
 * <p>
 * 41-bit的时间可以表示（1L<<41）/(1000L*3600*24*365)=69年的时间，10-bit机器可以分别表示1024台机器。
 * 如果我们对IDC划分有需求，还可以将10-bit分5-bit给IDC，分5-bit给工作机器。这样就可以表示32个IDC，每个IDC下可以有32台机器，
 * 可以根据自身需求定义。12个自增序列号可以表示2^12个ID，理论上snowflake方案的QPS约为409.6w/s，
 * 这种分配方式可以保证在任何一个IDC的任何一台机器在任意毫秒内生成的ID都是不同的。
 * <p>
 * 优点：
 * 毫秒数在高位，自增序列在低位，整个ID都是趋势递增的。
 * 不依赖数据库等第三方系统，以服务的方式部署，稳定性更高，生成ID的性能也是非常高的。
 * 可以根据自身业务特性分配bit位，非常灵活。
 * <p>
 * 缺点：
 * 强依赖机器时钟，如果机器上时钟回拨，会导致发号重复或者服务会处于不可用状态。
 *
 * @author : qiuzh
 * @version : v1.0.0
 * @desc : 全局唯一序列生成类
 * @since : 2018-01-13 11:23
 */
public class IdWorker {

    private final static long twepoch = 1454700000149L;

    /**
     * 机器标识位数
     */
    private final static long workerIdBits = 4L;

    /**
     * 机器ID最大值
     */
    private final static long maxWorkerId = -1L ^ -1L << workerIdBits;

    /**
     * 毫秒内自增位
     */
    private final static long sequenceBits = 10L;

    /**
     * 机器ID偏左移10位
     */
    private final static long workerIdShift = sequenceBits;

    /**
     * 时间毫秒左移14位
     */
    private final static long timestampLeftShift = sequenceBits + workerIdBits;

    private final static long sequenceMask = -1L ^ -1L << sequenceBits;

    private long lastTimestamp = -1L;

    private final long workerId;

    private long sequence = 0L;

    //private final AtomicBoolean lock = new AtomicBoolean(false);

    private IdWorker(final long workerId) {
        super();
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        this.workerId = workerId;
    }

    private static class IdWorkerHolder {
        private final static IdWorker instance = new IdWorker(0);
    }

    /**
     * 取得IdWorker的单例实现
     *
     * @return
     */
    public static IdWorker getInstance() {
        return IdWorkerHolder.instance;
    }

    public synchronized long nextId() {
        long timestamp = this.timeGen();
        // 如果是同一时间生成的，则进行毫秒内序列
        if (this.lastTimestamp == timestamp) {
            // 当前毫秒内，则+1
            this.sequence = (this.sequence + 1) & sequenceMask;
            // 毫秒内序列溢出
            if (this.sequence == 0) {
                // 当前毫秒内计数满了，则等待下一秒,获得新的时间戳
                System.out.println("###########" + sequenceMask);
                timestamp = this.tilNextMillis(this.lastTimestamp);
            }
        } else {
            // 时间戳改变，毫秒内序列重置
            this.sequence = 0;
        }
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < this.lastTimestamp) {
            try {
                throw new Exception(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", this.lastTimestamp - timestamp));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 上次生成ID的时间截
        this.lastTimestamp = timestamp;
        // ID偏移组合生成最终的ID，并返回ID
        long nextId = ((timestamp - twepoch << timestampLeftShift)) | (this.workerId << workerIdShift) | (this.sequence);
        //System.out.println("单据号序列生成[timestamp:" + timestamp + ",timestampLeftShift:" + timestampLeftShift + ",nextId:" + nextId + ",workerId:" + workerId + ",sequence:" + sequence + "]");
        return nextId;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    private long tilNextMillis(final long lastTimestamp) {
        long timestamp = this.timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = this.timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }

    //public static void main(String[] args) {
    //    for (int i = 0; i < 10; i++) {
    //        System.out.println(getInstance().nextId());
    //    }
    //}
}
