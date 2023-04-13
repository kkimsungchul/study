package service;



public class Paging {
	int totalCount;			//총 회원의 수
	int totalPage;			//총 페이지 수
	int startPage=1;		//화면에 표시 할 시작페이지의 번호 기본적으로 시작페이지는 1번부터
	int endPage=10;			//화면에 표시 할  마지막 페이지의 번호 기본적으로 마지막페이지는 10, 총 수가 안된다면 그에따라서 감소함
	int displayQuantity=10;	//화면에 표시 할 회원의 수
	int curPage;			//현재 선택된 페이지 번호;

	
	//생성자를사용하여 페이징 클래스를 선언할때 총페이지수와 현재페이지 2개의 값만 넘겨주면, 나머지 값들을 다 계산해서 쓸수있게 구현
	public Paging(int totalCount, int curPage) {
		this.setTotalCount(totalCount);
		this.setCurPage(curPage);
		this.setTotalPage(this.totalPage(totalCount));
		this.setStartPage(this.startPage(curPage,this.getTotalPage()));
		this.setEndPage(this.endPage(curPage,this.getTotalPage()));
		
	}
	
	//총 페이지 수를 구함
	public int totalPage(int totalCount) {
        if(totalCount%displayQuantity==0){
            this.totalPage=totalCount/displayQuantity;
        }else{
            this.totalPage=totalCount/displayQuantity+1;
        }

		return totalPage;
	}
	
	//화면에 보여줄 시작페이지를 구함
	public int startPage(int curPage, int totalPage) {
		if(curPage>5 && totalPage>10) {
			startPage=curPage-4;
			if(totalPage-curPage<5){
				startPage=totalPage-9;
			}
		}
		return startPage;
	}
	
	//화면에 보여줄 마지막페이지를 구함
	public int endPage(int curPage,int totalPage) {
		if(totalPage>10) {
			if(curPage>5) {
				endPage=curPage+5;
				if(totalPage-curPage<5){
					endPage=totalPage;
				}
			}
		}else {
			endPage=totalPage;
		}	
		return endPage;
	}
	
	//이 밑으로는 겟터 셋터 
	public int getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	public int getTotalPage() {
		return totalPage;
	}
	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}
	public int getStartPage() {
		return startPage;
	}
	public void setStartPage(int startPage) {
		this.startPage = startPage;
	}
	public int getEndPage() {
		return endPage;
	}
	public void setEndPage(int endPage) {
		this.endPage = endPage;
	}
	public int getDisplayQuantity() {
		return displayQuantity;
	}
	public void setDisplayQuantity(int displayQuantity) {
		this.displayQuantity = displayQuantity;
	}
	public int getCurPage() {
		return curPage;
	}
	public void setCurPage(int curPage) {
		this.curPage = curPage;
	}



	@Override
	public String toString() {
		return "Paging [totalCount=" + totalCount + ", totalPage=" + totalPage + ", startPage=" + startPage
				+ ", endPage=" + endPage + ", displayQuantity=" + displayQuantity + ", curPage=" + curPage + "]";
	}

	
	
	
	
	
	

	
	
	
	
	

}
