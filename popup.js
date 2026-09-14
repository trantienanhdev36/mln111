const API_URL = 'https://mln111-1-8ezh.onrender.com';
const analyzeButton = document.getElementById('analyzeButton');
const statusElement = document.getElementById('status');

function setStatus(message, isError = false) {
  statusElement.textContent = message;
  statusElement.classList.toggle('error', isError);
}

async function getCurrentTabContent(tab) {
  const [result] = await chrome.scripting.executeScript({
    target: { tabId: tab.id },
    func: () => ({
      title: document.title,
      url: window.location.href,
      content: document.body ? document.body.innerText : ''
    })
  });

  return result.result;
}

async function analyzeCurrentTab() {
  analyzeButton.disabled = true;
  setStatus('Đang đọc nội dung trang...');

  try {
    const [currentTab] = await chrome.tabs.query({ active: true, currentWindow: true });
    if (!currentTab || !currentTab.id) {
      throw new Error('Không tìm thấy tab hiện tại.');
    }

    const pageData = await getCurrentTabContent(currentTab);
    if (!pageData.content.trim()) {
      throw new Error('Trang hiện tại không có nội dung văn bản để phân tích.');
    }

    setStatus('Đang gửi nội dung đến máy chủ...');
    const response = await fetch(`${API_URL}/analyze`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(pageData)
    });

    if (!response.ok) {
      throw new Error(`Máy chủ trả về lỗi HTTP ${response.status}.`);
    }

    const html = await response.text();
    const resultUrl = URL.createObjectURL(new Blob([html], { type: 'text/html' }));
    await chrome.tabs.create({ url: resultUrl });
    window.close();
  } catch (error) {
    setStatus(error.message || 'Không thể phân tích trang hiện tại.', true);
    analyzeButton.disabled = false;
  }
}

analyzeButton.addEventListener('click', analyzeCurrentTab);
