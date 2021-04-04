package group.bison.data.data_layer.drill.domain.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by diaobisong on 2020/7/5.
 */
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = -275582248840137389L;
    private Long count;
    private int code;
    private Long currentPage;
    private Long pageSize;
    private List<T> data;

    public static <T> PageResult.PageResultBuilder<T> builder() {
        return new PageResult.PageResultBuilder();
    }

    public Long getCount() {
        return this.count;
    }

    public int getCode() {
        return this.code;
    }

    public Long getCurrentPage() {
        return this.currentPage;
    }

    public Long getPageSize() {
        return this.pageSize;
    }

    public List<T> getData() {
        return this.data;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setCurrentPage(Long currentPage) {
        this.currentPage = currentPage;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(!(o instanceof PageResult)) {
            return false;
        } else {
            PageResult other = (PageResult)o;
            if(!other.canEqual(this)) {
                return false;
            } else {
                label63: {
                    Long this$count = this.getCount();
                    Long other$count = other.getCount();
                    if(this$count == null) {
                        if(other$count == null) {
                            break label63;
                        }
                    } else if(this$count.equals(other$count)) {
                        break label63;
                    }

                    return false;
                }

                if(this.getCode() != other.getCode()) {
                    return false;
                } else {
                    label55: {
                        Long this$currentPage = this.getCurrentPage();
                        Long other$currentPage = other.getCurrentPage();
                        if(this$currentPage == null) {
                            if(other$currentPage == null) {
                                break label55;
                            }
                        } else if(this$currentPage.equals(other$currentPage)) {
                            break label55;
                        }

                        return false;
                    }

                    Long this$pageSize = this.getPageSize();
                    Long other$pageSize = other.getPageSize();
                    if(this$pageSize == null) {
                        if(other$pageSize != null) {
                            return false;
                        }
                    } else if(!this$pageSize.equals(other$pageSize)) {
                        return false;
                    }

                    List this$data = this.getData();
                    List other$data = other.getData();
                    if(this$data == null) {
                        if(other$data != null) {
                            return false;
                        }
                    } else if(!this$data.equals(other$data)) {
                        return false;
                    }

                    return true;
                }
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof PageResult;
    }

    public int hashCode() {
        boolean PRIME = true;
        byte result = 1;
        Long $count = this.getCount();
        int result1 = result * 59 + ($count == null?43:$count.hashCode());
        result1 = result1 * 59 + this.getCode();
        Long $currentPage = this.getCurrentPage();
        result1 = result1 * 59 + ($currentPage == null?43:$currentPage.hashCode());
        Long $pageSize = this.getPageSize();
        result1 = result1 * 59 + ($pageSize == null?43:$pageSize.hashCode());
        List $data = this.getData();
        result1 = result1 * 59 + ($data == null?43:$data.hashCode());
        return result1;
    }

    public String toString() {
        return "PageResult(count=" + this.getCount() + ", code=" + this.getCode() + ", currentPage=" + this.getCurrentPage() + ", pageSize=" + this.getPageSize() + ", data=" + this.getData() + ")";
    }

    public PageResult() {
    }

    public PageResult(Long count, int code, Long currentPage, Long pageSize, List<T> data) {
        this.count = count;
        this.code = code;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.data = data;
    }

    public static class PageResultBuilder<T> {
        private Long count;
        private int code;
        private Long currentPage;
        private Long pageSize;
        private List<T> data;

        PageResultBuilder() {
        }

        public PageResult.PageResultBuilder<T> count(Long count) {
            this.count = count;
            return this;
        }

        public PageResult.PageResultBuilder<T> code(int code) {
            this.code = code;
            return this;
        }

        public PageResult.PageResultBuilder<T> currentPage(Long currentPage) {
            this.currentPage = currentPage;
            return this;
        }

        public PageResult.PageResultBuilder<T> pageSize(Long pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public PageResult.PageResultBuilder<T> data(List<T> data) {
            this.data = data;
            return this;
        }

        public PageResult<T> build() {
            return new PageResult(this.count, this.code, this.currentPage, this.pageSize, this.data);
        }

        public String toString() {
            return "PageResult.PageResultBuilder(count=" + this.count + ", code=" + this.code + ", currentPage=" + this.currentPage + ", pageSize=" + this.pageSize + ", data=" + this.data + ")";
        }
    }
}
